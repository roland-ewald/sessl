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

/**
 * Simple default implementation to handle parameters.
 *
 * @see OptimizationParameters
 *
 * @author Roland Ewald
 */
case class SimpleParameters(val params: Map[String, Any]) extends OptimizationParameters with Logging {

  override def apply(s: String): Any = {
    val rt = params.get(s)
    if (!rt.isDefined)
      logger.error("No value defined for parameter '" + s + "'")
    rt.get
  }

}