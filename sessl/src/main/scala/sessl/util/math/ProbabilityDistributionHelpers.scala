/*******************************************************************************
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
 ******************************************************************************/
package sessl.util.math

/**
 * An interface that provides a collection of helpers for probability distributions.
 *
 * @author Roland Ewald
 */
trait ProbabilityDistributionHelpers {

  /**
   * Calculate quantiles of the chi-square function.
   * Implementations should behave similar to R's qchisq function.
   *
   * @param prob the probability
   * @param prob the degrees of freedom
   * @param tail whether P(X <= x) is calculated (default) or P(X > x)
   */
  def chiSquareQuantiles(prob: Double, degOfFreedom: Double, tail: Boolean = true): Double

}