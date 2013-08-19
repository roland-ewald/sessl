/**
 * *****************************************************************************
 * Copyright 2013 ALeSiA Team
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
 * ****************************************************************************
 */
package sessl.util.math

/**
 * Some auxiliary math functions.
 *
 * @author Roland Ewald
 */
object Misc {

  /**
   * Compute mean squared error (MSE) for two sequences of values.
   * @param d1 the first sequence
   * @param d2 the second sequence
   */
  def mse(s1: Seq[Double], s2: Seq[Double]): Double = {
    require(s1.size == s2.size, s"""Data sets must be of same size, but first set contains ${s1.size} elements and second one ${s2}.""")
    val errors = for ((recorded, original) <- s1 zip s2) yield Math.pow(recorded.doubleValue - original, 2)
    errors.sum / errors.size
  }

  def rmse(numbers: Seq[Double], comparisonData: Seq[Double]): Double = Math.sqrt(mse(numbers, comparisonData))

}