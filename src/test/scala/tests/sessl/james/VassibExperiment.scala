package tests.sessl.james

import org.junit.Test
import sessl.james.Experiment
import sessl.james.Instrumentation
import sessl.james.NextReactionMethod
import sessl.james.ParallelExecution
import sessl.james.PerformanceObservation
import sessl.james.Report
import sessl.james.TauLeaping
import sessl.execute
import sessl.stringToDataElementName
import sessl.stringToVarName
import sessl.AbstractInstrumentation
import sessl.AllSimulators
import sessl.range
import sessl.tools.TrajectorySetsComparator
import sessl.tools.CSVFileWriter
import sessl.util.AlgorithmSet

/** Simple experiment to produce some test data for VASSiB.
 *  @author Roland Ewald
 */
@Test class VassibExperiment {

  /** Run SR-Repressilator experiment. */
  @Test def runSRRepressilatorExperiment = {

    import sessl._
    import sessl.james._

    val refModelOutput = CSVFileWriter("vassib_autoreg_nw_mlr_reference.csv")
    val runtimes = CSVFileWriter("vassib_autoreg_nw_mlr_runtimes.csv")
    val tlUncertainty = CSVFileWriter("vassib_autoreg_nw_mlr_comparison.csv")

    val repsForReferenceImpl = 20
    val repsForEvaluation = 20

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Instrumentation with ParallelExecution {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      stopTime = 20500
      bind("P2", "P", "RNA")
      observeAt(range(0, 20, 20000))
      parallelThreads = -1
    }

    //Execute reference experiment
    var referenceResult: InstrumentationReplicationsResultsAspect = null
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
    simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.01, 0.02)))
    //simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.002, 0.05), "gamma" <~ range(5, 1, 15)))

    //Execute accuracy experiments
    for (sim <- simulators.algorithms)
      execute {
        new AutoRegExperiment with PerformanceObservation {
          replications = repsForEvaluation
          simulator = sim
          withReplicationsResult { result =>
            tlUncertainty << (sim, TrajectorySetsComparator.compare(referenceResult, result, "P2"))
          }
          withReplicationsPerformance { result =>
            runtimes << (sim, result.runtimes)
          }
        }
      }
  }
}