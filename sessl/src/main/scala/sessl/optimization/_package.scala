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
package sessl

package object optimization {

  /** The objective function mabs arbitrarily many parameters to some Double value. */
  type ObjectiveFunction[-X <: AbstractObjective] = (OptimizationParameters, X) => Unit

  /** The optimization 'command'. */
  def optimize[X <: AbstractObjective](o: X)(f: ObjectiveFunction[X]): InitializedObjectiveFunction[X] = InitializedObjectiveFunction(o, f)

  /** Short-hand notation for single-objective maximization. */
  def maximize(f: ObjectiveFunction[SingleObjective]): InitializedObjectiveFunction[SingleObjective] = optimize(SingleObjective(max))(f)

  /** Short-hand notation for single-objective minimization. */
  def minimize(f: ObjectiveFunction[SingleObjective]): InitializedObjectiveFunction[SingleObjective] = optimize(SingleObjective(min))(f)

  /** Types to declare which objective shall be minimized and which shall be maximized. */
  sealed trait OptDirection
  case object min extends OptDirection
  case object max extends OptDirection
}