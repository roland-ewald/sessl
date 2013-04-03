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
 * Super class for all optimizer bindings.
 * @author Roland Ewald
 */
abstract class AbstractOptimizerSetup {

  /** The objective function to be used. */
  private[this] var objective: Option[Objective] = None

  def setObjective(f: Objective) {
    require(!objective.isDefined, "Objective is already defined.")
    objective = Some(f)
  }

  def execute = {
    println("here be dragons (using " + objective.get + " :)")
  }
}