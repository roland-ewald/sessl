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

  //TODO: Move to utility class and test this!

  def getInterpolationIndices(results: MultiTable): List[(Int, Int)] = {

    if (observationTimes.isEmpty) return List()
    val timePoints = results.getTimePoints()
    if (timePoints.isEmpty) return List()

    val timePointIndices = ListBuffer[(Int, Int)]()
    var j = 0
    for (i <- 1 until timePoints.length) {
      while (j < observationTimes.length && timePoints(i) > observationTimes(j)) {
        timePointIndices += ((i - 1, i))
        j += 1
      }
    }

    timePointIndices.toList
  }

}

object Instrumentation {
  val MIN_INSTRUMENTATION_PRECISION = 10e-02
}