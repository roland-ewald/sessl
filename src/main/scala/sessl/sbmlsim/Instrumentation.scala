package sessl.sbmlsim

import org.simulator.math.odes.MultiTable

import sessl.util.SimpleInstrumentation

/** Support for 'instrumentation' of SBMLsimulator runs.
 *  It seems the simulators always provide the complete state vector for every computed step,
 *  so this here just implements some kind of sessl-compliant cherry-picking.
 *  @author Roland Ewald
 */
trait Instrumentation extends SimpleInstrumentation with ResultHandling {
  this: Experiment =>

  abstract override def considerResults(runId: Int, assignmentId: Int, results: MultiTable) {
    super.considerResults(runId, assignmentId, results)
    //call def addValueFor[T](runID: Int, internalName: String, value: TimeStampedData)
  }

}