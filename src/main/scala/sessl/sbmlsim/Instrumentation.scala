package sessl.sbmlsim

import org.simulator.math.odes.MultiTable
import sessl.util.SimpleInstrumentation
import scala.collection.mutable.ListBuffer

/** Support for 'instrumentation' of SBMLsimulator runs.
 *  It seems the simulators always provide the complete state vector for every computed step,
 *  so this here just implements some kind of sessl-compliant cherry-picking.
 *  @author Roland Ewald
 */
trait Instrumentation extends SimpleInstrumentation with ResultHandling {
  this: Experiment =>

  abstract override def considerResults(runId: Int, assignmentId: Int, results: MultiTable) {
    super.considerResults(runId, assignmentId, results)

    //    println(getInterpolationIndices(results))

    for (varName <- varsToBeObserved) {
      val index = 0
      //      while (iterator.nonEmpty) {
      //        val obstime = iterator.next()
      //
      //        for (obsTime <- observationTimes) {
      //          addValueFor(runId, varName, (obsTime, 1.0))
      //        }
      //      }
    }
  }


}

object Instrumentation {
  val MIN_INSTRUMENTATION_PRECISION = 10e-02
}