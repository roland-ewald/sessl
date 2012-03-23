package tests.sessl.james

import org.junit.Test
import org.junit.Assert._

/** Some example experiments to be used in talk(s) and publication(s).
 *
 *  @author Roland Ewaldd
 */
@Test class Examples {

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

  /** Performance observation and testing example. */
  @Test def testPerfEvalAndReporting {

    import sessl._
    import sessl.james._

    val tlSetups = TauLeaping() scan
      ("epsilon" <~ range(.02, .01, .05))
    val nrSetups = NextReactionMethod() scan
      ("eventQueue" <~ (Heap, SortedList))
    execute {
      new Experiment with ParallelExecution with Report with PerformanceObservation {
        model = "java://examples.sr.LinearChainSystem"
        replications = 200; stopTime = 1.5
        simulators <~ tlSetups; simulators <~ nrSetups
        performanceDataSink = FilePerformanceDataSink()
        withExperimentPerformance { r => //withRunPerf etc. ...
          reportSection("Results") {
            histogram(r.runtimes)(title = "All run times")
            boxPlot(r.runtimesForAll)(title = "Time/setup")
            boxPlot(r.runtimesFor(tlSetups))(title = "Time/setup")
          }
        }
      }
    }
  }

  @Test def testExperimentReuse {

    import sessl._
    import sessl.james._

    class MyLCSExperiment extends Experiment with ParallelExecution {
      model = "java://examples.sr.LinearChainSystem"
      replications = 2
    }

    execute {
      new MyLCSExperiment() {
        stopCondition = AfterSimSteps(100)
      }
    }
  }

  @Test def testExperimentNesting {

    import sessl._
    import sessl.james._

    class MyLCSExperiment extends Experiment with ParallelExecution {
      model = "java://examples.sr.LinearChainSystem"
      replications = 2
    }

    var subCounter = 0
    var counter = 0
    execute {
      new MyLCSExperiment() {
        stopCondition = AfterSimSteps(100)
        afterRun {
          r =>
            {
              counter += 1
              execute {
                new MyLCSExperiment() {
                  stopCondition = AfterSimSteps(10)
                  afterRun {
                    r => subCounter += 1
                  }
                }
              }
            }
        }
      }
    }

    assertEquals(4, subCounter)
    assertEquals(2, counter)
  }

  @Test def testExperimentAdaptation {

    import sessl._
    import sessl.james._

    execute {
      new Experiment {
        model = "java://examples.sr.LinearChainSystem"
        replications = 2; stopTime = 1
        exp.setBackupEnabled(true) //<-add custom code here
        exp.getExecutionController().setExperiment(exp)
      }
    }

  }

  @Test def testIntrodctionExperiment {
    import sessl._
    import sessl.james._

    execute {
      new Experiment with Instrumentation with ParallelExecution {
        model = "file-sr:/./SimpleModel.sr"
        scan("r1" <~ range(.5, .1, 1.4))
        replications = 10
        stopCondition = AfterWallClockTime(seconds = 1) and AfterSimTime(1.0)
        bind("x" ~ "A")
        observeAt(range(0.1, .01, .9))
        withRunResult {
          results => println(results ~ "x")
        }
      }
    }
  }

}