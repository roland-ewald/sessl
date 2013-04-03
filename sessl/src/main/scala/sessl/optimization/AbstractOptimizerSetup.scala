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
  private[this] var obj: Option[Objective] = None

  /** The search space consists of an arbitrary number of dimensions.*/
  private[this] val searchSpaceDims = ListBuffer[SearchSpaceDimension[_]]()

  //TODO: add constraints

  def objective = obj.get

  lazy val searchSpace = searchSpaceDims.toList

  /** Store the objective function (must not be called more than once).*/
  def setObjective(f: Objective) {
    require(!obj.isDefined, "Objective is already defined.")
    obj = Some(f)
  }

  /** Executes the optimization task.*/
  def execute()

  def param[X](name: String, values: Iterable[X]) = searchSpaceDims += GeneralSearchSpaceDimension[X](name, values)

  def param[X <: Numeric[X]](name: String, lowerBound: X, upperBound: X) = searchSpaceDims += BoundedSearchSpaceDimension[X](name, lowerBound, upperBound)

}

/** Represents a dimension in the search space. */
trait SearchSpaceDimension[+X] {
  def name: String
}

/** A dimension in the search space defined by an explicit list of values. */
case class GeneralSearchSpaceDimension[X](name: String, values: Iterable[X]) extends SearchSpaceDimension[X]

/** A dimension in the search space defined by boundaries. */
case class BoundedSearchSpaceDimension[X <: Numeric[X]](name: String, lowerBound: X, upperBound: X) extends SearchSpaceDimension[X]