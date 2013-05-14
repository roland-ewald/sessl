/**
 * *****************************************************************************
 * Copyright 2013 Roland Ewald
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
package sessl.optimization

import sessl.util.Logging
import scala.collection.generic.MutableSetFactory

/**
 * Simple default implementation to handle parameters.
 *
 * @see OptimizationParameters
 *
 * @author Roland Ewald
 */
case class SimpleParameters(val params: Map[String, Any]) extends OptimizationParameters with Logging {

  /** The parameter names. */
  val paramNames = params.keys.toList

  /** Some (arbitrary) numbering of the parameters, based on their order in `paramNames´. */
  val parameterIndices = paramNames.zipWithIndex.toMap

  /** Array with flags for unused parameters. */
  private[this] val unusedParams = Range(0, params.size).map(_ => true).toArray

  override def apply[X](s: String): X = {
    val param = params.get(s)
    if (!param.isDefined)
      throw new IllegalArgumentException("No value defined for parameter '" + s + "'")
    unusedParams(parameterIndices(s)) = false
    param.get.asInstanceOf[X]
  }

  /** Returns index of first unused parameter, or -1 if none exists. */
  def firstUnusedParameter = unusedParams.indexWhere(x => x)

  def firstUnusedParameterName: Option[String] = {
    val idx = firstUnusedParameter
    if (idx < 0)
      None
    else Some(paramNames(idx))
  }

}