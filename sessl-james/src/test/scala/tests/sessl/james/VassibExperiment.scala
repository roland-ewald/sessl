package tests.sessl.james

import org.junit.Test
import sessl.james.Experiment
import sessl.james.Observation
import sessl.james.NextReactionMethod
import sessl.james.ParallelExecution
import sessl.james.PerformanceObservation
import sessl.james.Report
import sessl.james.TauLeaping
import sessl.execute
import sessl.stringToDataElementName
import sessl.stringToVarName
import sessl.AbstractObservation
import sessl.AllSimulators
import sessl.range
import sessl.util.AlgorithmSet
import sessl.james.tools.CSVFileWriter
import sessl.james.tools.TrajectorySetsComparator

/** Simple experiment to produce some test data for VASSiB.
 *  @author Roland Ewald
 */
@Test class VassibExperiment {

  def firstExperiment = {

    import sessl._
    import sessl.james._

    val refModelOutput = CSVFileWriter("vassib_autoreg_nw_sr_reference.csv")
    val runtimes = CSVFileWriter("vassib_autoreg_nw_sr_runtimes.csv")
    val tlUncertainty = CSVFileWriter("vassib_autoreg_nw_sr_comparison.csv")

    val repsForReferenceImpl = 1000
    val repsForEvaluation = 250

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Observation with ParallelExecution {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      stopTime = 20500
      observe("P2") //, "P", "RNA"
      observeAt(range(0, 20, 20000))
      parallelThreads = -1
    }

    //Execute reference experiment
    var referenceResult: ObservationReplicationsResultsAspect = null
    execute {
      new AutoRegExperiment {
        replications = repsForReferenceImpl
        simulator = NextReactionMethod()
        withRunResult(refModelOutput << _.values("P2"))
        withReplicationsResult(referenceResult = _)
      }
    }

    require(referenceResult != null, "No reference result recorded!")

    val simulators = new AlgorithmSet[Simulator]()
    simulators <+ NextReactionMethod()
    //    simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.01, 0.02)))
    simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.002, 0.05), "gamma" <~ range(5, 1, 15)))

    //Execute accuracy experiments
    for (sim <- simulators.algorithms)
      execute {
        new AutoRegExperiment with PerformanceObservation {
          replications = repsForEvaluation
          simulator = sim
          withReplicationsResult { result =>
            //Maybe use KL-Divergence, or some other measure/test?
            tlUncertainty << (sim, TrajectorySetsComparator.compare(referenceResult, result, "P2"))
          }
          withReplicationsPerformance { result =>
            runtimes << (sim, result.runtimes)
          }
        }
      }
  }

  @Test def secondExperiment = {

    import sessl._
    import sessl.james._

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Observation with ParallelExecution {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      replications = 1000
      stopTime = 10500
      observe("P2")
      observeAt(range(0, 20, 10000))
      parallelThreads = -1
    }

    val simulators = new AlgorithmSet[Simulator]()
    simulators <~ {
      {
        TauLeaping() scan (
          "epsilon" <~ range(0.01, 0.002, 0.05),
          "gamma" <~ range(5, 1, 15),
          "criticalReactionThreshold" <~ range(0, 5, 45))
      } ++ {
        NextReactionMethod() scan {
          "eventQueue" <~ (Heap(), SortedList())
        }
      }
    }

    //Write trajectories and run times for each simulator
    val runtimes = CSVFileWriter("vassib_autoreg_nw_sr_runtimes.csv")
    for (sim <- simulators.algorithms) {
      val modelOutput = CSVFileWriter("vassib_autoreg_nw_sr_trajectories_" + sim + ".csv")
      execute {
        new AutoRegExperiment with PerformanceObservation {
          simulator = sim
          withRunResult(modelOutput << _.values("P2"))
          withReplicationsPerformance { result =>
            runtimes << (sim, result.runtimes)
          }
        }
      }
    }
  }
}
object VassibExperiment {
  def main(args: Array[String]): Unit = {
    (new VassibExperiment).secondExperiment
  }
}