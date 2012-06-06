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