package tests.sessl.james

import org.junit.Test

/** Simple experiment to produce some test data for VASSiB.
 *  @author Roland Ewald
 */
@Test class VassibExperiment {

  /** Run SR-Repressilator experiment. */
  @Test def runSRRepressilatorExperiment = {

    import sessl._
    import sessl.james._

    val repsForReferenceImpl = 3
    val repsForTauImpl = 5

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Instrumentation with ParallelExecution with Report {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      stopTime = 10100
      bind("P2", "P", "RNA")
      observeAt(range(0, 20, 10000))
      parallelThreads = -1
    }

    //Execute reference experiment
    var referenceResult: Any = None
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

    //Execute accuracy experiment
    execute {
      new AutoRegExperiment /*TODO with PerformanceObservation*/ {
        replications = repsForTauImpl
        simulators <~ (TauLeaping() scan ("epsilon" <~ range(0.01, 0.002, 0.05), "gamma" <~ range(5, 1, 15)))
        simulatorExecutionMode = AllSimulators
        reportName = "Accuracy Results"
        /*TODO: withReplicationsPerformance(
         Crossvalidator.compare(perf.resultsForSetup(s),referenceResult)
        */
      }
    }
  }
}