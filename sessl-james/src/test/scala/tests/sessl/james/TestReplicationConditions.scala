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

/** Tests the replication conditions.
 *  @author Roland Ewald
 */
@Test class TestReplicationConditions {

  @Test def testReplicationConditions() = {

    import sessl._
    import sessl.james._

    val manyReps = 20

    var numberOfReps = -1

    // Combine replication conditions via 'and'
    val expAnd = new Experiment with Observation with ParallelExecution {
      model = TestJamesExperiments.testModel
      stopTime = 1.5
      observe("x" ~ "S3")
      observeAt(0.45) 
      //replicationCondition = FixedNumber(1) and (FixedNumber(manyReps) or MeanConfidenceReached("x")) TODO: implement this for James II...
      replicationCondition = FixedNumber(1) and FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expAnd)
    assertEquals("The number of replications should match", manyReps, numberOfReps)

    // Combine replication conditions via 'or'
    val expOr = new Experiment with Observation with ParallelExecution {
      model = TestJamesExperiments.testModel
      stopTime = 0.5
      observe("x" ~ "S3")
      observeAt(0.45)
      replicationCondition = FixedNumber(1) or FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expOr)
    assertEquals("The number of replications should match", 1, numberOfReps)
  }
}
