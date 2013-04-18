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
package sessl.util

import scala.collection.mutable.ListBuffer

/** Support for simple interpolation tasks.
 *  @author Roland Ewald
 */
object Interpolation {

  /** A pair of index and corresponding time value, useful when working with list of sample time points. */
  type IndexValue = (Int, Double)

  /** Triple (index+time of lower boundary, time to be observed, index+time of upper boundary).
   *  The lower boundary is the largest recorded sample time smaller than the time at which
   *  the state shall be observed. Vice versa, the upper bound is the smallest recorded sample
   *  time larger than the time at which the state shall be observed.
   */
  type InterpolationPoint = (IndexValue, Double, IndexValue)

  /** Finds interpolation points for a sequence of time points at which data has been recorded and a list
   *  of time points for which data would be required.
   *  Note that it is assumed that timesToObserve is *sorted* (in increasing order).
   *  @param recordedSampleTimes the recorded times
   *  @param timesToObserve the times at which the system shall be observed (in increasing order)
   *  @return a list containing an interpolation point for each time to be observed (in the correct order)
   */
  def findInterpolationPoints(recordedSampleTimes: Seq[Double], timesToObserve: Seq[Double]): List[InterpolationPoint] = {
    require(recordedSampleTimes.size >= 2, "Only one time recorded, interpolation not possible.")
    require(timesToObserve.nonEmpty, "No times to be observed are given.")
    require(recordedSampleTimes.head <= timesToObserve.head && recordedSampleTimes.last >= timesToObserve.last,
      "Not all desired observation times (" + timesToObserve.mkString(",") +
        ") are covered by time point interval [" + recordedSampleTimes.head + "," +
        "]. Ignoring observation configuration entirely.")
    createInterpolationPoints(recordedSampleTimes, timesToObserve)
  }

  /** Creates a list of interpolation points (see findInterpolationPoints()). */
  private[this] def createInterpolationPoints(availableSampleTimes: Seq[Double], observeSampleTimes: Seq[Double]): List[InterpolationPoint] = {

    var sampleIndex = 0
    var obsTimeIndex = 0
    val timePointIndices = ListBuffer[InterpolationPoint]()

    //Iterate over both sequences at the same time, increasing one index per iteration (depending on the situation)
    while (sampleIndex < availableSampleTimes.length - 1 && obsTimeIndex < observeSampleTimes.length) {
      val previousRecordedTime = availableSampleTimes(sampleIndex)
      val nextRecordedTime = availableSampleTimes(sampleIndex + 1)
      val timeForWhichToObserve = observeSampleTimes(obsTimeIndex)
      if (nextRecordedTime >= timeForWhichToObserve) {
        timePointIndices += (((sampleIndex, previousRecordedTime), timeForWhichToObserve, (sampleIndex + 1, nextRecordedTime)))
        obsTimeIndex += 1
      } else {
        sampleIndex += 1
      }
    }
    timePointIndices.toList
  }

}
