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
package tests.sessl.omnetpp

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import sessl.reference.Report
import scala.collection.mutable.ListBuffer

/**
 * Some tests for the OMNeT++ binding.
 *
 *  @author Roland Ewald
 */
@Test class SimpleOMNeTPPExperiments {

  @Test(expected = classOf[UnsupportedOperationException])
  def testCallToInvalidModelSetter() {
    assumeTrue(testEnvironmentIsSuitable)

    import sessl._
    import sessl.omnetpp._

    execute {
      new Experiment {
        model = "tictoc.exe"
      }
    }
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInvalidModel() {
    assumeTrue(testEnvironmentIsSuitable)

    import sessl._
    import sessl.omnetpp._

    execute {
      new Experiment {
        model = ("tictoc.exe" -> "TictocNetworkThatDoesNotExist")
      }
    }
  }

  @Test def testTicTocExperiment() {
    assumeTrue(testEnvironmentIsSuitable)

    import sessl._
    import sessl.omnetpp._

    var resultCounter = 0
    execute {
      new Experiment with EventLogRecording with Observation {
        model = ("omnetpp-samples/tictoc/tictoc.exe" -> "Tictoc1")
        replications = 2
        set("Network.host[*].app.typename" <~ "TicTocApp")
        stopCondition = AfterSimTime(minutes = 10) or AfterWallClockTime(seconds = 10)
        scan("tic.out.delay" <~ range(100, 100, 200), "tic.in.delay" <~ range(20, 20, 100) and "Network.numHosts" <~ range(20, 20, 100))
        afterRun {
          r => resultCounter += 1
        }
      }
    }
    assertEquals(20, resultCounter)
  }

  @Test def testClosedQueueingNetwork() {
    assumeTrue(testEnvironmentIsSuitable)

    import sessl._
    import sessl.omnetpp._

    val recordedTrajectories = ListBuffer[Trajectory]()
    val q1VarName = "length_Q1"
    val q2VarName = "ClosedQueueingNetA.queue[2].queueLength"
    val observationRange = range(1000, 100, 30000)

    if (testEnvironmentIsSuitable)
      execute {
        new Experiment with Observation {
          model = ("omnetpp-samples/cqn/cqn.exe" -> "ClosedQueueingNetA")
          warmup = Duration(seconds = 20)
          observeAt(observationRange)
          observe(q1VarName ~ "ClosedQueueingNetA.queue[0].queueLength", "ClosedQueueingNetA.queue[*].queueLength")
          set("*.numTandems" <~ 2, "*.numQueuesPerTandem" <~ 3)
          replications = 2
          stopCondition = AfterSimTime(hours = 10) or AfterWallClockTime(seconds = 10)
          scan("*.queue[*].numInitialJobs" <~ (2),
            "*.sDelay" <~ range("%ds", 2, 2, 8) and "*.qDelay" <~ range("%ds", 2, 2, 8) and "*.queue[*].serviceTime" <~ range("exponential(%ds)", 2, 2, 8))
          withRunResult { r =>
            recordedTrajectories += (r ~ q1VarName)._2
            recordedTrajectories += (r ~ q2VarName)._2
            logger.info("Some recorded values: " + (r ~ q2VarName)._2.take(10))
          }
        }
      }

    recordedTrajectories.toList.foreach(t => assertEquals(observationRange.toList.length, t.length))

    // If corresponding binding is included, this works as well (when mixing in sessl.james.Report):
    //        reportName = "OMNeT++ Report"
    //        withRunResult { r =>
    //          {
    //            reportSection("Run Number " + r.id) {
    //              linePlot(r ~ "length_Q1", r ~ "length_Q5")(title = "Queue lengths")
    //            }
    //          }
    //        }
  }

  /** Checks whether the tests are running on Windows. */
  val testEnvironmentIsSuitable =
    Seq(("os.name", "Windows 7"), ("os.arch", "amd64")) forall { t => java.lang.System.getProperty(t._1).equals(t._2) }

}
