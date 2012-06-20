package tests.sessl.omnetpp

import org.junit.Test

/**
 * Some tests for the OMNeT++ binding.
 *
 *  @author Roland Ewald
 */
@Test class SimpleOMNeTPPExperiments {

  @Test(expected = classOf[UnsupportedOperationException])
  def testCallToInvalidModelSetter() {
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
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment {
        model = ("tictoc.exe" -> "TictocNetworkThatDoesNotExist")
      }
    }
  }

  @Test def testTicTocExperiment() {
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment with EventLogRecording with Observation {
        model = ("omnetpp-samples/tictoc/tictoc.exe" -> "Tictoc1")
        replications = 2
        set("Network.host[*].app.typename" <~ "TicTocApp")
        stopCondition = AfterSimTime(minutes = 10) or AfterWallClockTime(seconds = 10)
        scan("tic.out.delay" <~ range(100, 100, 1000), "tic.in.delay" <~ range(20, 20, 100) and "Network.numHosts" <~ range(20, 20, 100))
      }
    }
  }

  @Test def testClosedQueueingNetwork() {
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment with EventLogRecording with Observation {
        model = ("omnetpp-samples/cqn/cqn.exe" -> "ClosedQueueingNetA")
        replications = 2
        stopCondition = AfterSimTime(hours = 10) or AfterWallClockTime(seconds = 10)
        scan("*.queue[*].numInitialJobs" <~ (2, 4, 8),
          "*.sDelay" <~ range("%ds", 2, 2, 10) and "*.queue[*].serviceTime" <~ range("exponential(%ds)", 2, 2, 10))
      }
    }
  }

}