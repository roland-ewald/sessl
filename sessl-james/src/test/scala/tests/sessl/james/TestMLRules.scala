package tests.sessl.james

import org.junit.Assert._
import org.junit.Test
import sessl.execute
import sessl.james.Experiment
import sessl.james.MLRulesReference
import sessl.util.Logging
import sessl.util.MiscUtils
import java.io.File

class TestMLRules extends Logging {

  import sessl._
  import sessl.james._

  val testDir = "./testOutputData"

  /** Create test experiment. */
  def testExperiment = new Experiment with Observation {
    model = "file-mlrj:/./EndoExoCytosis.mlrj"
    simulator = MLRulesReference()
    stopTime = 0.1
    observeAt(range(0.0, 0.001, 0.095))
  }

  /** Create test experiment with simple observable declaration. */
  def simpleObservationExperiment = {
    val exp = testExperiment
    exp.observe("A", "B")
    exp
  }

  /** Create test experiment with more complex observables. */
  def attributeHierarchyObservationExperiment = {
    val exp = testExperiment
    exp.observe("A" ~ "A()", "B" ~ "Cell/Vesicle()")
    exp
  }

  /** Tests whether both observation modes are supported. */
  @Test def testObservationModes = {
    testMLRulesObservation(simpleObservationExperiment)
    testMLRulesObservation(attributeHierarchyObservationExperiment)
  }

  /** Tests whether observer output is written to file if (and only if) the user specified an output directory. */
  @Test def testOutputDirMLRulesObservation = {

    def outputDataDirExists = new File(testDir).exists

    MiscUtils.deleteRecursively(testDir)
    testMLRulesObservation(simpleObservationExperiment)
    assertFalse(outputDataDirExists)

    val exp = simpleObservationExperiment
    exp.observationOutputDirectory = testDir
    testMLRulesObservation(exp)
    assertTrue(outputDataDirExists)
  }

  /** Executes experiment and checks whether results have been observed. */
  def testMLRulesObservation[E <: Experiment with Observation](exp: E) = {
    var trajectoryB: Option[Trajectory] = None
    exp.withRunResult(results => {
      logger.info("Results of A:" + results.trajectory("A"))
      trajectoryB = Some(results.trajectory("B"))
    })
    execute(exp)
    assertTrue(trajectoryB.isDefined)
    assertEquals(trajectoryB.get.size, exp.observationTimes.length)
  }

}