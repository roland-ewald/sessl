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
  type ObjectiveFunction = (OptimizationParameters, Objective) => Unit

  /** The optimization 'command'. */
  def optimize(o: Objective)(f: ObjectiveFunction): InitializedObjectiveFunction = InitializedObjectiveFunction(o, f)

  //min & max
  sealed trait OptDirection
  case object min extends OptDirection
  case object max extends OptDirection
}