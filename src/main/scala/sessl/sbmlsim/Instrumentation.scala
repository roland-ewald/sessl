package sessl.sbmlsim

import sessl.AbstractInstrumentation
import sessl.InstrumentationRunResultsAspect
import sessl.InstrumentationReplicationsResultsAspect
import sessl.util.SimpleInstrumentation

/** Support for 'instrumentation' of SBMLsimulator runs.
 *  It seems the simulators always provide the complete state vector for every computed step,
 *  so this here just implements some kind of sessl-compliant cherry-picking.
 *  @author Roland Ewald
 */
trait Instrumentation extends SimpleInstrumentation {
  this: Experiment =>

  def process() {
    //call def addValueFor[T](runID: Int, internalName: String, value: TimeStampedData)
  }

}