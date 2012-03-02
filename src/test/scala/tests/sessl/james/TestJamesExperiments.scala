package tests.sessl.james

import james.core.util.StopWatch
import sessl.AbstractExperiment

/** Auxiliary constants and methods for testing the James II bindings for sessl.
 *  @author Roland Ewald
 */
object TestJamesExperiments {

  /** Model that fills the test counter object. */
  val testCounterModel = "java://tests.sessl.james.BogusLCSModel"

  /** Default test model. */
  val testModel = "java://examples.sr.LinearChainSystem"

  /** Measures execution time of an experiment. */
  def measureExecTime(exp: AbstractExperiment) = {
    val sw = new StopWatch()
    sw.start();
    AbstractExperiment.execute(exp);
    sw.stop();
    sw.elapsedMilliseconds()
  }
}