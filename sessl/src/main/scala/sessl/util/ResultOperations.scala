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

import sessl.Trajectory

/** Provides support for some basic operations on result data.
 *  @author Roland Ewald
 */
trait ResultOperations {

  /** Retrieves the data for a variable name.*/
  protected def getValuesFor(varName: String): Iterable[_]

  /** Get the maximum value of a variable. */
  def max(name: String): Double = aggregate(name, Double.NegativeInfinity, scala.math.max)

  /** Get the minimum value of a variable. */
  def min(name: String): Double = aggregate(name, Double.PositiveInfinity, scala.math.min)

  /** Get the absolute minimum value of a variable. */
  def absmin(name: String): Double = aggregate(name, Double.PositiveInfinity, (x, y) => scala.math.min(scala.math.abs(x), scala.math.abs(y)))

  /** Get the absolute maximum value of a variable. */
  def absmax(name: String): Double = aggregate(name, 0, (x, y) => scala.math.max(scala.math.abs(x), scala.math.abs(y)))

  /** Gets the mean value of a variable. */
  def mean(name: String): Double = {
    val numOfSamples: Double = getValuesFor(name).size
    aggregate(name, 0, (x, y) => x + y / numOfSamples)
  }

  /** Gets the (unbiased) standard deviation of a variable. */
  def stddev(name: String): Double = scala.math.sqrt(variance(name))

  /** Gets the (unbiased) variance of a variable. */
  def variance(name: String): Double = {
    val m = mean(name)
    val denominator: Double = getValuesFor(name).size - 1
    require(denominator > 0, "Variance cannot be estimated based on a single value!")
    aggregate(name, 0, (x, y) => { x + scala.math.pow(y - m, 2) / denominator })
  }

  /** Aggregates variable values by a given function.
   *
   *  @param name
   *          the name
   *  @param startVal
   *          the start value
   *  @param aggregator
   *          the aggregation function (applied in a left fold)
   *  @return the aggregated result
   */
  private def aggregate(name: String, startVal: Double, aggregator: (Double, Double) => Double): Double = {
    val vals = getValuesFor(name)
    require(vals.head.isInstanceOf[Number], "This operation is only available for numeric types, but at least one value of '" + name + "' is of type " + vals.head.getClass())
    val numbers = vals.asInstanceOf[List[Number]]
    (startVal /: numbers)((x, y) => aggregator(x, y.doubleValue()))
  }
}
