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

    execute {
      new Experiment with Instrumentation with ParallelExecution {
        model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
        stopTime = 10100
        bind("P2", "P", "RNA")
        observeAt(range(0, 10, 10000))
        withRunResult {
          r => println(r ~ "P")
        }
        parallelThreads = -1
      }
    }
  }
}