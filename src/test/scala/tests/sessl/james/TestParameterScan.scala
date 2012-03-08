package tests.sessl.james

import tests.sessl.TestCounter
import org.junit.Test
import org.junit.Assert._

/** Tests parameter scanning experiment with some extras.
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
    val exp = new Experiment with Instrumentation with ParallelExecution {

      model = TestJamesExperiments.testCounterModel
      stopTime = 1.0
      replications = 2

      rng = MersenneTwister(1234)

      define("fixedVar" ==> fixedValue, "answer" ==> "no!")

      scan("upperVar" ==> (1, 2), { "testDouble" ==> range(1., 1., 10.) } and { "testInt" ==> range(21, 1, 30) } + { "testLong" ==> range(31L, 1L, 40L) })

      observeAtTimes(.1, .2, .3, .9) { // currently no true 'scope', but would be possible...
        bind("x" to "S1", "y" ~ "S1")
      }

      withRunResult { //specifies what shall be done after each run
        results =>
          {
            println("Last y-value:" + results("y"))
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
            println("y-results:" + results("y"))
            println("Variance of y-results:" + results.variance("y"))
            TestCounter.checkEquality("The size without restrictions should always be the same.", replications * expectedSetups, results("y").size)
            TestCounter.checkEquality("The given restriction should not restrict anything.", replications * expectedSetups, results.having("fixedVar" ==> fixedValue)("y").size)
            TestCounter.checkEquality("Exactly one configuration should have upperVar=1, testInt=25:", replications, results.having("upperVar" ==> 1, "testInt" ==> 25)("y").size)
          }
      }

      // Note that system-specific configuration is ALWAYS possible, too! 
      exp.getFixedModelParameters.put("theAnswer", new java.lang.Integer(42))

      parallelThreads = -1 //Leave one core idle... 
      // Simple configuration tasks are done by case classes...
      simulator = DirectMethod
    }

    execute(exp)

    //Checking results
    TestCounter.checkValidity(exp.replications * expectedSetups, paramCounterMap => paramCounterMap.size == expectedSetups && paramCounterMap.forall(mapEntry => mapEntry._2 == exp.replications))
    assertEquals("ReplicationsDone should be called as often as there are setups.", expectedSetups, repDoneCounter)
    assertEquals("Only one experiment has been finished.", 1, expDoneCounter)
  }

}