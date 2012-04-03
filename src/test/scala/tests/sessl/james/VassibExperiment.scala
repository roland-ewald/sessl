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

/** Simple experiment to produce some test data for VASSiB.
 *  @author Roland Ewald
 */
@Test class VassibExperiment {

  /** Run SR-Repressilator experiment. */
  @Test def runSRRepressilatorExperiment = {

    import sessl._
    import sessl.james._

    val repsForReferenceImpl = 20
    val repsForTauImpl = 20

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Instrumentation with ParallelExecution with Report {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      stopTime = 10100
      bind("P2", "P", "RNA")
      observeAt(range(0, 20, 10000))
      parallelThreads = -1
    }

    //Execute reference experiment
    var referenceResult: InstrumentationReplicationsResultsAspect = null
    execute {
      new AutoRegExperiment {
        replications = repsForReferenceImpl
        simulator = NextReactionMethod()
        reportName = "Reference Results"
        withRunResult {
          r => linePlot(r)(title = "Test run " + r.id)
        }
        withReplicationsResult(referenceResult = _)
      }
    }

    require(referenceResult != null, "No reference result recorded!")

    //Execute accuracy experiment
    execute {
      new AutoRegExperiment with PerformanceObservation {
        replications = repsForTauImpl
        simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.01, 0.03) /*, "gamma" <~ range(5, 1, 15)*/ ))
        simulatorExecutionMode = AllSimulators
        reportName = "Accuracy Results"
        withReplicationsPerformance { results =>
          for (s <- simulators.algorithms) {
            println("Results: " +
              TrajectorySetsComparator.compare(referenceResult, results.forSetupsAndAspect(s, new InstrumentationReplicationsResultsAspect()), "P2"))
          }
        }
      }
    }
  }
}