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

  @Test def testSimpleExperiment() {
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment {
        model = ("omnetpp-sample/tictoc.exe" -> "Tictoc1")
        replications = 2
        set("Network.host[*].app.typename" <~ "TicTocApp")
        stopCondition = AfterSimTime(hours = 1000) or AfterWallClockTime(seconds = 10)
        scan("tic.out.dely" <~ range(100, 100, 1000), "tic.in.delay" <~ range(20, 20, 100) and "Network.numHosts" <~ range(20, 20, 100))
      }
    }
  }

}