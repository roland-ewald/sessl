package sessl.sbw

import sessl.util.SimpleObservation
import sessl.util.Interpolation._

trait Observation extends SimpleObservation with SBWResultHandling {
  this: Experiment =>
   
  abstract override def considerResults(runId: Int, assignmentId: Int, 
      species: Array[String], results: Array[Array[Double]]) {
    super.considerResults(runId, assignmentId, species, results)
    for (v <- varsToBeObserved) {
      val index = species.indexOf(v)
      if (index < 0) {
        logger.warn("Variable '" + v + "' could not be found, will be ignored.\nVariables defined in the model: " + species.mkString(", "))
      } else {
        val timepoints = results.map{r => r.apply(0)}
        findInterpolationPoints(timepoints, observationTimes).foreach(p => addValueForPoint(runId, p, index, v, results))
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
  private[this] def addValueForPoint(runId: Int, point: InterpolationPoint, colIndex: Int, varName: String, results: Array[Array[Double]]) = {
    require(point._1._2 <= point._2 && point._3._2 >= point._2, "Invalid interpolation point:" + point)

    //Get values between which shall be interpolated
    val firstValue = results.apply(point._1._1).apply(colIndex)
    val secondValue = results.apply(point._3._1).apply(colIndex)

    //Calculate width of time interval
    val timeInterval = point._3._2 - point._1._2

    //Calculate weights
    val firstWeight = (point._3._2 - point._2) / timeInterval
    val secondWeight = 1 - firstWeight

    addValueFor(runId, varName, (point._2, firstWeight * firstValue + secondWeight * secondValue))
  }
}