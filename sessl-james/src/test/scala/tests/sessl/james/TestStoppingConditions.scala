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
