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

import scala.collection.mutable.ListBuffer

/**
 * Super class for all optimizer bindings.
 *
 * @example {{{
 * minimize {
 * //...
 * } using SomeOptSetup { //<- Sub-class of this class
 * // Configuration
 * }
 * }}}
 *
 * @author Roland Ewald
 */
abstract class AbstractOptimizerSetup {

  /** The objective function to be used. */
  private[this] var objFunction: Option[ObjectiveFunction[_]] = None

  /** The objective (manages result values). */
  private[this] var obj: Option[Objective] = None

  /** The search space consists of an arbitrary number of dimensions.*/
  private[this] val searchSpaceDims = ListBuffer[SearchSpaceDimension[_]]()

  /** The actions to be done after the objective function was evaluated once. */
  private[this] val evaluationDoneActions = ListBuffer[SingleSolutionAction]()

  /** The actions to be done after an iteration of the optimization algorithm is done. */
  private[this] val iterationDoneActions = ListBuffer[MultipleSolutionsAction]()

  /** The actions to be done after the optimization procedure is done. */
  private[this] val optimizationDoneActions = ListBuffer[MultipleSolutionsAction]()

  /** The actions to be done with the best results, after each iteration. */
  private[this] val resultsOfIterationActions = ListBuffer[MultipleSolutionsAction]()

  /** The actions to be done with the best results, after the optimization. */
  private[this] val resultsOfOptimizationActions = ListBuffer[MultipleSolutionsAction]()

  //TODO: add pre/post-constraints

  /** The objective function. */
  def objectiveFunction = objFunction.get

  /** The objective. */
  def objective = obj.get

  /** The overall search space, consisting of all dimensions (parameters) that have been defined. */
  lazy val searchSpace = searchSpaceDims.toList

  /** Store the objective function (must not be called more than once).*/
  def setObjectiveFunction(f: ObjectiveFunction[_ <: Objective]) {
    require(!objFunction.isDefined, "Objective function is already defined.")
    objFunction = Some(f)
  }

  /** Store the objective */
  def setObjective(o: Objective) {
    require(!obj.isDefined, "Objective is already defined.")
    obj = Some(o)
  }

  /** Executes the optimization task.*/
  def execute()

  /**
   * Add parameter with a name and additional values.
   *  @param name parameter name
   *  @param values set of values the parameter can take (any type)
   */
  def param[X](name: String, values: Iterable[X]) = {
    checkParamName(name)
    searchSpaceDims += GeneralSearchSpaceDimension[X](name, values)
  }

  /**
   * Add numerical parameter with a name, bounds, and a step size.
   *  @param name parameter name
   *  @param lowerBound the lower bound
   *  @param stepSize the step size
   *  @param upperBound the upper bound
   */
  def param[X <: AnyVal](name: String, lowerBound: X, stepSize: X, upperBound: X)(implicit n: Numeric[X]) = {
    checkParamName(name)
    searchSpaceDims += BoundedSearchSpaceDimension[X](name, lowerBound, stepSize, upperBound)
  }

  /**
   * Execute a given function after the objective function has been evaluated.
   *  @param f the function
   */
  def afterEvaluation(f: SingleSolutionAction) = { evaluationDoneActions += f }

  /**
   * Get all actions to be executed after an evaluation.
   *  @return the list of actions to be executed after each evaluation
   */
  protected def afterEvaluationActions = evaluationDoneActions.toList

  /**
   * Execute a given function after an iteration of the optimization algorithm is done.
   *  @param f the function
   */
  def afterIteration(f: MultipleSolutionsAction) = { iterationDoneActions += f }

  /**
   * Get all actions to be executed after an iteration.
   *  @return the list of actions to be executed after each iteration
   */
  protected def afterIterationActions = iterationDoneActions.toList

  /**
   * Execute a given function after the optimization algorithm is finished.
   *  @param f the function
   */
  def afterOptimization(f: MultipleSolutionsAction) = { optimizationDoneActions += f }

  /**
   * Get all actions to be executed after the optimization.
   *  @return the list of actions to be executed after the optimization
   */
  protected def afterOptimizationActions = optimizationDoneActions.toList

  /**
   * Execute a given function on the best results, after each iteration.
   *  @param f the function
   *  @example{{{
   *  withIterationResults { optResults =>
   *   println(optResults)
   *  }
   *  }}}
   */
  def withIterationResults(f: MultipleSolutionsAction) = { resultsOfIterationActions += f }

  /**
   * Get all actions to be executed with the results of an iteration.
   *  @return the list of actions to be executed on the result of an iteration
   */
  protected def iterationResultActions = resultsOfIterationActions.toList

  /**
   * Execute a function on the best results, after the whole optimization is finished.
   *  @param f the function
   *  @example{{{
   *  withOptimizationResults { optResults =>
   *   println(optResults)
   *  }
   *  }}}
   */
  def withOptimizationResults(f: MultipleSolutionsAction) = { resultsOfOptimizationActions += f }

  /**
   * Get all actions to be executed with the overall optimization results.
   *  @return the list of actions to be executed on the overall results of the optimization
   */
  protected def optimizationResultActions = resultsOfOptimizationActions.toList

  /** Checks whether this parameter name has already been used. */
  private[this] def checkParamName(name: String) {
    require(!searchSpaceDims.exists(_.name == name), "Parameter with name '" + name + "' has been defined twice!")
  }

}

/** Represents a dimension in the search space. */
trait SearchSpaceDimension[+X] {
  def name: String
}

/** A dimension in the search space defined by an explicit list of values. */
case class GeneralSearchSpaceDimension[X](name: String, values: Iterable[X]) extends SearchSpaceDimension[X]

/** A dimension in the search space defined by boundaries. */
case class BoundedSearchSpaceDimension[X <: AnyVal](name: String, lowerBound: X, stepSize: X, upperBound: X)(implicit n: Numeric[X]) extends SearchSpaceDimension[X] {
  val interval = n.toDouble(n.minus(upperBound, lowerBound))
  val numSteps = math.round(interval / n.toDouble(stepSize))
}
