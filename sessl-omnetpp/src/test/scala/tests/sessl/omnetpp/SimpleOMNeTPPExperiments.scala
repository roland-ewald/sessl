package tests.sessl.omnetpp

import org.junit.Test

/** Some tests for the OMNeT++ binding.
 *
 *  @author Roland Ewald
 */
@Test class SimpleOMNeTPPExperiments {

  @Test(expected = classOf[IllegalArgumentException])
  def testInvalidModel() {
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment {
        model = "test"
      }
    }
  }

  @Test def testSimpleExperiment() {
    import sessl._
    import sessl.omnetpp._
    execute {
      new Experiment {
        model = "./test.bat"
        set("Network.numHosts" <~ 15, "Network.host[*].app.typename" <~ "PingApp")
        stopCondition = AfterSimTime(hours = 10) or AfterWallClockTime(seconds = 10)
      }
    }
  }

}