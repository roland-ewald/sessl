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
 * @author Roland Ewald
 */
abstract class AbstractOptimizerSetup {

  /** The objective function to be used. */
  private[this] var objFunction: Option[ObjectiveFunction[_]] = None

  private[this] var obj: Option[AbstractObjective] = None

  /** The search space consists of an arbitrary number of dimensions.*/
  private[this] val searchSpaceDims = ListBuffer[SearchSpaceDimension[_]]()

  //TODO: add constraints

  /** Thhe objective function. */
  def objectiveFunction = objFunction.get

  def objective = obj.get

  /** The overall search space, consisting of all dimensions (parameters) that have been defined. */
  lazy val searchSpace = searchSpaceDims.toList

  /** Store the objective function (must not be called more than once).*/
  def setObjectiveFunction(f: ObjectiveFunction[_ <: AbstractObjective]) {
    require(!objFunction.isDefined, "Objective function is already defined.")
    objFunction = Some(f)
  }

  def setObjective(o: AbstractObjective) {
    require(!obj.isDefined, "Objective is already defined.")
    obj = Some(o)
  }

  /** Executes the optimization task.*/
  def execute()

  /** Add parameter with a name and additional values. */
  def param[X](name: String, values: Iterable[X]) = {
    checkParamName(name)
    searchSpaceDims += GeneralSearchSpaceDimension[X](name, values)
  }

  /** Add numerical parameter with a name, bounds, and a step size. */
  def param[X <: AnyVal](name: String, lowerBound: X, stepSize: X, upperBound: X)(implicit n: Numeric[X]) = {
    checkParamName(name)
    searchSpaceDims += BoundedSearchSpaceDimension[X](name, lowerBound, stepSize, upperBound)
  }

  /** Checks whether this parameter name has already been used. */
  private[this] def checkParamName(name: String) {
    require(!searchSpaceDims.exists(_ == name), "Parameter with name '" + name + "' has been defined twice!")
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
