package tests.sessl.sbmlsim

import org.junit.Test
import org.junit.Assert._

/** Tests for the integration of SBMLsimulator.
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

  @Test def testSimpleExperiment() {
    import sessl._
    import sessl.sbmlsim._
    var runCounter = 0
    var replicationCounter = 0
    val exp = new Experiment with ParallelExecution /*with Instrumentation */ {

      model = "./BIOMD0000000002.xml"
      define("kr_0" ==> 8042)
      scan("kf_2" ==> range(30000, 1000, 34000), "kr_2" ==> (650, 750))
      stopTime = 1000.0

      simulatorSet << (Euler() scan { "stepSize" ==> range(0.01, 0.01, 0.1) })
      afterRun { r => { runCounter += 1 } }
      afterReplications { r => { replicationCounter += 1 } }
    }
    execute(exp)
    assertEquals("There should be 100 runs.", 100, runCounter)
    assertEquals("There should be 10 variable assignments", 10, replicationCounter)

  }
}