package sessl.sbw

import org.junit.Assert._
import org.junit.Test
import sessl.sbw._
import sessl._
import analysis.singlerun.steadystate.estimator.mser.MSERSteadyStateEstimator

@Test class TestExperiment {

  @Test def testSBWExecution() = {
    execute(
      {
        new Experiment with Observation with ParallelExecution with SteadyStateEstimation {
//          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbmlsim/src/test/resources/BIOMD0000000002.xml"
          set("MKKK" <~ 100.0)
          replications = 1
//          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbw/src/test/resources/dsmts-001-01.xml"
//          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbw/src/test/resources/BIOMD0000000001_SBML-L2V1.xml"
//          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbw/src/test/resources/BIOMD0000000008_SBML-L2V1.xml"
          model = "C:/Users/Stefan/Repositories/SESSL/sessl-sbw/src/test/resources/BIOMD0000000010_SBML-L2V3.xml"
          stopTime = 100.0
          observe("x" ~ "MKKK")
          observeAt(0.1)
          estimateSteadyState(new MSERSteadyStateEstimator(0.5), "x_steady" ~ "x")
        }
      })
  }
  
}