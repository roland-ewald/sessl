package sessl.sbw

import org.junit.Assert._
import org.junit.Test
import sessl.sbw._
import sessl._

@Test class TestExperiment {

  @Test def testSBWExecution() = {
    execute(
      {
        new Experiment {
//          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbmlsim/src/test/resources/BIOMD0000000002.xml"
          set("Lambda" <~ 0.1)
          replications = 1
          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbw/src/test/resources/dsmts-001-01.xml"
          stopTime = 10000.0
          //...
        }
      })
  }
  
}