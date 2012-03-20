package tests.sessl.james

import org.junit.Test

/** Some example experiments from a presentation.
 *
 *  @author Roland Ewaldd
 */
@Test class TalkExamples {

  /** Full instrumentation example. */
  @Test def testInstrumentation {

    import sessl._
    import sessl.james._
    execute {
      new Experiment with Instrumentation {
        model = "java://examples.sr.LinearChainSystem"
        replications = 10; stopTime = 1.0
        scan("numOfSpecies" <~ (10, 15))
        bind("x" to "S1", "y" ~ "S5")
        observeAt(range(.0, .1, .9))

        withRunResult {
          result => println("Last y-value:" + result("y"))
        }
        withReplicationsResult {
          result => println("Last y-values:" + result("y"))
        }
        withExperimentResult {
          result =>
            {
              println("Overall variance:" + result.variance("y"))
              println("Variance on subset:" + result.having("numOfSpecies" <~ 10).variance("y"))
            }
        }
      }
    }
  }

}