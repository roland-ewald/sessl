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
 * Provides the `using(...)` method to combine the definition of the
 * objective function ([[ObjectiveFunction]]) and the configuration of the
 * optimization tool ([[AbstractOptimizerSetup]]).
 *
 * See [[AbstractOptimizerSetup]].
 *
 * @author Roland Ewald
 */
case class InitializedObjectiveFunction[X <: Objective](o: X, f: ObjectiveFunction[X]) {

  /**
   * Configures the given optimizer setup with the objective function, then executes it.
   *  @param setup the optimizer setup
   */
  def using(setup: AbstractOptimizerSetup) = {
    setup.setObjective(o)
    setup.setObjectiveFunction(f)
    setup.execute
  }
}