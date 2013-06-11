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
import sessl.util.test.TestCounter

/**
 * Tests parameter scanning experiment with some extras.
 *  @author Roland Ewald
 */
@Test class TestParameterScan {

  @Test def test {
    val expectedSetups = 20
    val fixedValue = 345

    import sessl._
    import sessl.james._

    var repDoneCounter = 0
    var expDoneCounter = 0

    TestCounter.reset()
    val exp = new Experiment with Observation with ParallelExecution {

      model = TestJamesExperiments.testCounterModel
      stopTime = 1.0
      replications = 2

      rng = MersenneTwister(1234)

      set("fixedVar" <~ fixedValue, "answer" <~ "no!")

      scan("upperVar" <~ (1, 2), "testDouble" <~ range(1.0, 1.0, 10.0) and "testInt" <~ range(21, 1, 30) and "testLong" <~ range(31L, 1L, 40L))

      observe("x" to "S1", "y" ~ "S1") // currently no true 'scope' (e.g. observe x only for t \in [l,u]), but would be possible...
      observeAt(.1, .2, .3, .9)

      withRunResult { //specifies what shall be done after each run
        results =>
          {
            logger.info("Last y-value:" + results("y"))
          }
      }

      withReplicationsResult { //specifies what shall be done after all runs for one variable assignment have been completed
        results =>
          {
            repDoneCounter += 1
            TestCounter.checkEquality("There should be exactly the number of desired replications (" + replications +
              ") in the result set, but there are: " + results("y").size, replications, results("y").size)
          }
      }

      withExperimentResult { //specifies what shall be done after experiment execution
        results =>
          {
            expDoneCounter += 1
            logger.info("y-results:" + results("y"))
            logger.info("Variance of y-results:" + results.variance("y"))
            TestCounter.checkEquality("The size without restrictions should always be the same.", replications * expectedSetups, results("y").size)
            TestCounter.checkEquality("The given restriction should not restrict anything.", replications * expectedSetups, results.having("fixedVar" <~ fixedValue)("y").size)
            TestCounter.checkEquality("Exactly one configuration should have upperVar=1, testInt=25:", replications, results.having("upperVar" <~ 1, "testInt" <~ 25)("y").size)
          }
      }

      // Note that system-specific configuration is ALWAYS possible, too! 
      exp.getFixedModelParameters.put("theAnswer", new java.lang.Integer(42))

      parallelThreads = -1 //Leave one core idle... 
      // Simple configuration tasks are done by case classes...
      simulator = DirectMethod()
    }

    execute(exp)

    //Checking results
    TestCounter.checkValidity(exp.replications * expectedSetups, paramCounterMap => paramCounterMap.size == expectedSetups && paramCounterMap.forall(mapEntry => mapEntry._2 == exp.replications))
    assertEquals("ReplicationsDone should be called as often as there are setups.", expectedSetups, repDoneCounter)
    assertEquals("Only one experiment has been finished.", 1, expDoneCounter)
  }

  @Test def testMinimalExample {
    import sessl._
    import sessl.james._

    execute {
      new Experiment {
        model = TestJamesExperiments.testCounterModel
        scan("x" <~ (1, 17), "y" <~ range(1.1, 1, 10.1) and "z" <~ range(21, 1, 30))
        stopTime = 1
        replications = 2
        rng = MersenneTwister(seed = 1234)
      }
    }
  }
}
