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

  @Test def testSimpleExperiments() {
    import sessl._
    import sessl.sbmlsim._
    var runCounter = 0
    var replicationCounter = 0
    class MyExperiment extends Experiment with ParallelExecution with Instrumentation {

      model = "./BIOMD0000000002.xml"
      set("kr_0" ==> 8042)
      scan("kf_2" ==> range(30000, 1000, 34000), "kr_2" ==> (650, 750))
      stopTime = .01
      observePeriodically(range(0, 1e-04, 1e-02)) { bind("x" ~ "ILL", "y" ~ "DLL") }

      simulatorSet << (DormandPrince54() scan { "stepSize" ==> (1e-06, 2e-06) })
      afterRun { r => { runCounter += 1 } }
      afterReplications { r => { replicationCounter += 1 } }
    }
    execute(new MyExperiment)
    assertEquals("There should be 20 runs.", 20, runCounter)
    assertEquals("There should be 10 variable assignments", 10, replicationCounter)

    //This experiment shows how to *combine* features of simulation systems!
    execute {
      new MyExperiment with sessl.james.Report {
        reportName = "SBMLsim Report"
        withRunResult { r =>
          {
            reportSection("Run Number " + r.id) {
              linePlot(r ~ "x", r ~ "y")(title = "Integration Results")
            }
          }
        }
      }
    }
  }
}