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

/**
 * Optimization parameters can have any type and are read via (unique) identifiers.
 * @author Roland Ewald
 */
trait OptimizationParameters {

  /**
   * Returns value of a parameter.
   *  @param s name of the parameter
   *  @return its value
   */
  def apply(s: String): Any

  /**
   * Returns value of a parameter with type to be inferred from the context.
   * @param s name of the parameter
   */
  def get[X](s: String): X = apply(s).asInstanceOf[X]

  /**
   * Returns all parameter values as a map.
   */
  def values: Map[String, Any]

}