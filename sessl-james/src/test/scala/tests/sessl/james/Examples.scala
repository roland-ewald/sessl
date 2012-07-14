/*******************************************************************************
 * Copyright 2012 Roland Ewald
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package tests.sessl.james

import org.junit.Test
import org.junit.Assert._
import sessl.util.Logging

/**
 * Some example experiments to be used in talk(s) and publication(s).
 *
 *  @author Roland Ewaldd
 */
@Test class Examples extends Logging {

  /** Observation example. */
  @Test def testObservation {

    import sessl._
    import sessl.james._
    execute {
      new Experiment with Observation {
        model = "java://examples.sr.LinearChainSystem"
        replications = 10; stopTime = 1.0
        scan("numOfSpecies" <~ (10, 15))
        observe("x" to "S1", "y" ~ "S5")
        observeAt(range(.0, .1, .9))

        withRunResult {
          result => logger.info("Last y-value:" + result("y"))
        }
        withReplicationsResult {
          result => logger.info("Last y-values:" + result("y"))
        }
        withExperimentResult {
          result =>
            {
              logger.info("Overall variance:" + result.variance("y"))
              logger.info("Variance on subset:" + result.having("numOfSpecies" <~ 10).variance("y"))
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
      ("eventQueue" <~ (Heap(), SortedList()))
    execute {
      new Experiment with ParallelExecution with Report with PerformanceObservation {
        model = "java://examples.sr.LinearChainSystem"
        replications = 200; stopTime = 1.5
        simulators <~ (tlSetups ++ nrSetups)
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
        exp.getExecutionController()
      }
    }

  }

  @Test def testIntrodctionExperiment {
    import sessl._
    import sessl.james._

    execute {
      new Experiment with Observation with ParallelExecution {
        model = "file-sr:/./SimpleModel.sr"
        scan("r1" <~ range(.5, .1, 1.4))
        replications = 10
        stopCondition = AfterWallClockTime(seconds = 1) and AfterSimTime(10e4)
        observe("A")
        observeAt(range(100, 50, 9000))
        withRunResult {
          results => logger.info((results ~ "A").toString)
        }
      }
    }
  }

  /** Testing a simple example experiment with to simulator setups. */
  @Test def testCrossvalidationTestPaper {

    import sessl._

    //Simulation system independent:
    trait SomeTestSetup {
      this: AbstractExperiment with AbstractObservation with AbstractReport =>
      model = "java://examples.sr.LinearChainSystem"
      replications = 200
      stopTime = 1.5
      observe("S3")
      observeAt(1.4)
      withExperimentResult { results =>
        reportSection("Results") {
          histogram(results("S3"))(title = "Species #3 after 1.4 s")
        }
      }
    }

    //Simulation system dependent:
    import sessl.james._

    class JamesIITestSetup(simulatorUnderTest: Simulator)
      extends Experiment with Observation with ParallelExecution with Report with SomeTestSetup {
      simulator = simulatorUnderTest
      reportName = "Results of " + simulator
    }

    val expDefaultSetup = new JamesIITestSetup(NextReactionMethod())
    val expHeapSetup = new JamesIITestSetup(NextReactionMethod(eventQueue = Heap()))
    //...

    execute(expDefaultSetup, expHeapSetup)
    someTest(expDefaultSetup.results, expHeapSetup.results)

    ///END
    def someTest(results1: ExperimentResults, results2: ExperimentResults) = {
      logger.info(results1.toString)
      logger.info(results2.toString)
    }
  }

  @Test def testPerfAnalysisExample {

    import sessl._
    import sessl.james._

    execute {
      new Experiment with ParallelExecution with PerformanceObservation with Report {
        model = "java://examples.sr.LinearChainSystem"
        stopTime = 1.5
        replications = 20

        val tlSetups = TauLeaping() scan ("epsilon" <~ range(0.02, 0.01, 0.05))
        val nrSetups = NextReactionMethod() scan {
          "eventQueue" <~ (BucketQueue(), LinkedList(), Heap(), SortedList())
        }

        simulators <~ (nrSetups ++ tlSetups)
        executionMode = AllSimulators

        performanceDataSink = FilePerformanceDataSink()

        reportName = "Sample Performance Report"
        withExperimentPerformance { r =>
          reportSection("Results") {
            boxPlot(r.runtimesForAll)("Run times for all setups")
            boxPlot(("TL", r.runtimes(tlSetups)), ("NRM", r.runtimes(nrSetups)))(
              title = "Run time comparison for algorithm families")
          }
        }
      }
    }
  }

}
