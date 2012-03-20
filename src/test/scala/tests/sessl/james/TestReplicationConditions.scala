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
    val expAnd = new Experiment with Instrumentation with ParallelExecution {
      model = TestJamesExperiments.testModel
      stopTime = 1.5
      bind("x" ~ "S3")
      observeAtTimes(0.45) 
      //replicationCondition = FixedNumber(1) and (FixedNumber(manyReps) or MeanConfidenceReached("x")) TODO: implement this for James II...
      replicationCondition = FixedNumber(1) and FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expAnd)
    assertEquals("The number of replications should match", manyReps, numberOfReps)

    // Combine replication conditions via 'or'
    val expOr = new Experiment with Instrumentation with ParallelExecution {
      model = TestJamesExperiments.testModel
      stopTime = 0.5
      bind("x" ~ "S3")
      observeAtTimes(0.45)
      replicationCondition = FixedNumber(1) or FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expOr)
    assertEquals("The number of replications should match", 1, numberOfReps)
  }
}