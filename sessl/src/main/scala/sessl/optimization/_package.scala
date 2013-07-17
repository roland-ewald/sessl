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

/**
 * Support for simulation-based optimization.
 * 
 * Optimizer configuration is handled in [[AbstractOptimizerSetup]].
 *
 * @example {{{
 * optimize(("throughput",max), ("errors", min)) {
 * (params, objective) =>
 * execute {
 *  new Experiment with Observation {
 * 	//... configure SESSL experiment, e.g.:
 *  set("modelParam" <~ params("optParam"))
 *  withRunResult{ result =>
 *  	objective("throughput") <~ result.mean("throughput")
 *   	objective("errors") <~ result.mean("errors")
 *   }
 *  }
 * }
 * } using new Opt4JSetup {
 * //Optimizer setup, e.g.:
 * param("optParam", 1, 10, 1000)
 * }
 * }}}
 */
package object optimization {

  /** The objective function maps arbitrarily many parameters to some values stored in the [[Objective]]. */
  type ObjectiveFunction[-X <: Objective] = (OptimizationParameters, X) => Unit

  /** Event handler considering a single parameter setup and the corresponding results of the objective function. */
  type SingleSolutionAction = (OptimizationParameters, Objective) => Unit

  /** Event handler considering multiple parameter setups and their corresponding results of the objective function. */
  type MultipleSolutionsAction = List[(OptimizationParameters, Objective)] => Unit

  /** The optimization 'command' (used for single-objective optimization). */
  def optimize[X <: Objective](o: X)(f: ObjectiveFunction[X]): InitializedObjectiveFunction[X] = InitializedObjectiveFunction(o, f)

  /**
   * Multi-objective optimization.
   *  @param dims list of tuples (dimension name, [min | max])
   *  @param f multi-objective function
   *  @return initialized optimization problem, to be simulation with an [[AbstractOptimizerSetup]]
   */
  def optimize(dims: (String, OptDirection)*)(f: ObjectiveFunction[MultiObjective]): InitializedObjectiveFunction[MultiObjective] = InitializedObjectiveFunction(MultiObjective(dims: _*), f)

  /**
   * Short-hand notation for single-objective maximization.
   * @example {{{
   * maximize {
   * (params, objective) =>
   * execute {
   *  new Experiment with Observation {
   * 	//...
   *  withRunResult{ result =>
   *  	objective <~ result.mean("throughput")
   *   }
   *  }
   * }
   * } using new Opt4JSetup {
   * //...
   * }
   * }}}
   */
  def maximize(f: ObjectiveFunction[SingleObjective]): InitializedObjectiveFunction[SingleObjective] = optimize(SingleObjective(max))(f)

  /**
   * Short-hand notation for single-objective minimization.
   * @example {{{
   * minimize {
   * (params, objective) =>
   * execute {
   *  new Experiment with Observation {
   * 	//...
   *  withRunResult{ result =>
   *  	objective <~ result.mean("throughput")
   *   }
   *  }
   * }
   * } using new Opt4JSetup {
   * //...
   * }
   * }}}
   */
  def minimize(f: ObjectiveFunction[SingleObjective]): InitializedObjectiveFunction[SingleObjective] = optimize(SingleObjective(min))(f)

  /** Types to declare which objective shall be minimized and which shall be maximized. */
  sealed trait OptDirection

  /** Represents minimization. */
  case object min extends OptDirection

  /** Represents maximization. */
  case object max extends OptDirection
}