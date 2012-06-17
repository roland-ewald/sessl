package sessl.sbmlsim

import org.simulator.math.odes.MultiTable
import sessl.util.ResultHandling

/**
 * Trait to let mix-ins handle results.
 *  @author Roland Ewald
 */
trait SBMLSimResultHandling extends ResultHandling {

  /**
   * Method to be overridden to extract data from the results.
   *  They use a lot of memory, so the results of each run will be discarded after calling this method.
   */
  protected[sbmlsim] def considerResults(runId: Int, assignmentId: Int, results: MultiTable) = { resultsHandlingIsCalled() }

}
