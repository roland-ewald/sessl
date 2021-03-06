/*******************************************************************************
 * Copyright 2012 Roland Ewald
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package sessl.sbmlsim

import org.simulator.math.odes.MultiTable
import sessl.util.SimpleObservation
import scala.collection.mutable.ListBuffer
import sessl.util.Interpolation._

/**
 * Support for observing of SBMLsimulator runs.
 *  It seems the simulators always provide the complete state vector for every computed step,
 *  so this here just implements some kind of sessl-compliant cherry-picking.
 *  @author Roland Ewald
 */
trait Observation extends SimpleObservation with SBMLSimResultHandling {
  this: Experiment =>

  abstract override def considerResults(runId: Int, assignmentId: Int, results: MultiTable) {
    super.considerResults(runId, assignmentId, results)
    for (v <- varsToBeObserved) {
      val colIndex = results.getColumnIndex(v)
      if (colIndex < 0) {
        logger.warn("Variable '" + v + "' could not be found, will be ignored.\nVariables defined in the model: " + retrieveVariableNames(results).mkString(","))
      } else {
        findInterpolationPoints(results.getTimePoints(), observationTimes).foreach(p => addValueForPoint(runId, p, colIndex, v, results))
      }
    }
  }

  /**
   * Adds the observed value for a given interpolation point.
   *  The overall width of the time interval is (t_2 - t_1), and the observation point (point._2)
   *  has to be somewhere in-between. The weights of the two considered sample values
   *  are adjusted accordingly (linear interpolation).
   *  @param runId the run id
   *  @param point the interpolation point
   *  @param colIndex the results column index
   *  @param varName the variable name
   *  @param results the results
   */
  private[this] def addValueForPoint(runId: Int, point: InterpolationPoint, colIndex: Int, varName: String, results: MultiTable) = {
    require(point._1._2 <= point._2 && point._3._2 >= point._2, "Invalid interpolation point:" + point)

    //Get values between which shall be interpolated
    val firstValue = results.getValueAt(point._1._1, colIndex)
    val secondValue = results.getValueAt(point._3._1, colIndex)

    //Calculate width of time interval
    val timeInterval = point._3._2 - point._1._2

    //Calculate weights
    val firstWeight = (point._3._2 - point._2) / timeInterval
    val secondWeight = 1 - firstWeight

    addValueFor(runId, varName, (point._2, firstWeight * firstValue + secondWeight * secondValue))
  }

  /** Retrieve variable names from results table. */
  private[this] def retrieveVariableNames(results: MultiTable): Seq[String] = {
    for (i <- 0 until results.getColumnCount) yield results.getColumnName(i)
  }

}
