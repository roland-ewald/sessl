package sessl.sbmlsim

import org.simulator.math.odes.MultiTable

/** Trait to let mix-ins handle results.
 *  @author Roland Ewald
 */
trait ResultHandling {

  /** Flag to check whether all mix-ins obeyed the contract and called their super method when overriding considerResults(...). */
  private[this] var considerResultsCalled = false

  /** Method to be overridden to extract data from the results.
   *  They use a lot of memory, so the results of each run will be discarded after calling this method.
   */
  protected[sbmlsim] def considerResults(runId: Int, assignmentId: Int, results: MultiTable) = { considerResultsCalled = true }

  /** Controls whether considerResults(...) has been correctly invoked. */
  def isConsiderResultsCalled = considerResultsCalled
}
