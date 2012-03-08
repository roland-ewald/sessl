package tests.sessl.james

import org.junit.Test
import org.junit.Assert._
import TestJamesExperiments._

/** Tests stopping conditions.
 *  @author Roland Ewald
 */
@Test class TestStoppingConditions {

  @Test def testStoppingConditions() = {

    import sessl._
    import sessl.james._

    //How to re-use experiment definitions: as normal classes...
    class DefaultExperiment extends Experiment with ParallelExecution {
      model = testModel
      replications = 2
    }

    execute(new DefaultExperiment() {
      stopCondition = Never or (AfterSimTime(0.5) and AfterSimSteps(1000)) //Never say 'Never() and ...' :)      
    })

    val exp1Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimSteps(0) })
    val exp2Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimSteps(10000) })
    val exp3Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimTime(0.5) })
    val exp4Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimTime(0.6) and AfterWallClockTime(seconds = 3) })

    assertTrue("Not simulating at all should always be the fastest.", exp1Time < exp2Time && exp1Time < exp3Time && exp1Time < exp4Time)
    assertTrue("", exp3Time < exp4Time)
  }
}