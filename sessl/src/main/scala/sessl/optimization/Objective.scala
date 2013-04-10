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
 * Data structure to manage values (objectives) returned from the objective function.
 * The values have to be real numbers.
 *
 * @see ObjectiveFunction
 * @see SingleObjective
 * @see MultiObjective
 *
 * @author Roland Ewald
 */
sealed trait Objective

/** Helper methods. */
object Objective {

  /**
   * Copies objective.
   *  @param o the objective
   *  @return a copy
   */
  def copy(o: Objective): Objective = o match {
    case single: SingleObjective => SingleObjective(single.direction)
    case multi: MultiObjective => MultiObjective(multi.dims: _*)
  }

}

/** */
case class SingleObjective(val direction: OptDirection) extends Objective {

  private[this] var value: Option[Double] = None

  def <~[X](newValue: X)(implicit n: Numeric[X]): Unit = {
    require(!value.isDefined, "The value of the objective function has already been set to " + value.get)
    value = Some(n.toDouble(newValue))
  }

  def singleValue = value.get
}

/** */
case class MultiObjective(val dims: (String, OptDirection)*) extends Objective {

  private val values = scala.collection.mutable.Map[String, Double]()

  val dimensionNames = dims.map(_._1)

  val dimensionDirections = dims.toMap

  def apply(name: String) = new AssignmentWrapper(name)

  class AssignmentWrapper(val name: String) {
    def <~[X](newValue: X)(implicit n: Numeric[X]): Unit = {
      require(!values.contains(name), "The value of the '" + name + "' objective has already been set to " + values(name))
      values(name) = n.toDouble(newValue)
    }
  }
}