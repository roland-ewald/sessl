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

  @Test def testSimpleExperiment() {

    import sessl._
    import sessl.sbmlsim._

    val exp = new Experiment /*with Instrumentation with ParallelExecution*/ {
      model = "./BIOMD0000000002.xml"
      stopTime = 1.0
    }
    //step size: 10e-05
    execute(exp)

  }
}