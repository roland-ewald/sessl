package tests.sessl.sbmlsim
import org.junit.Test

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
    val exp = new Experiment /*with Instrumentation with ParallelExecution*/ {
      model = "./BIOMD0000000002.xml"
      simulator = DormandPrince54(stepSize = 1e-02)
      stopTime = 1.0
    }
    execute(exp)

  }
}