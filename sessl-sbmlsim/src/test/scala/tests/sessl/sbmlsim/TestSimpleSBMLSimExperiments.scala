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
package tests.sessl.sbmlsim

import org.junit.Assert.assertEquals
import org.junit.Test

import sessl.execute
import sessl.stopConditionToCombinedCondition
import sessl.sbmlsim.Experiment
import sessl.AfterWallClockTime
import sessl.MeanConfidenceReached
import sessl.Never

/**
 * Tests for the integration of SBMLsimulator.
 *
 *  @author Roland Ewald
 */
@Test class TestSimpleSBMLSimExperiments {

  /** Setting a number of replications should not work, as deterministic solvers are provided. */
  @Test(expected = classOf[IllegalArgumentException])
  def testSetReplicationNumber() {
    import sessl._
    import sessl.sbmlsim._
    val exp = new Experiment {
      replications = 2
    }
  }

  /** Setting replication conditions should not work, as deterministic solvers are provided. */
  @Test(expected = classOf[IllegalArgumentException])
  def testSetReplicationConditions() {
    import sessl._
    import sessl.sbmlsim._
    val exp = new Experiment {
      replicationCondition = MeanConfidenceReached("x")
    }
  }

  /** Setting stopping conditions is not supported yet. */
  @Test(expected = classOf[UnsupportedOperationException])
  def testSetStoppingConditions() {
    import sessl._
    import sessl.sbmlsim._
    val exp = new Experiment {
      stopCondition = Never or AfterWallClockTime(seconds = 2)
    }
  }

  /** Tests a simple experiment. */
  @Test def testSimpleExperiments() {
    import sessl._
    import sessl.sbmlsim._

    var runCounter = 0
    var replicationCounter = 0

    class MyExperiment extends sessl.util.test.sbmlsim.SimpleTestExperiment {
      afterRun { r => { runCounter += 1 } }
      afterReplications { r => { replicationCounter += 1 } }
    }

    execute(new MyExperiment)
    assertEquals("There should be 20 runs.", 20, runCounter)
    assertEquals("There should be 10 variable assignments", 10, replicationCounter)
  }
}
