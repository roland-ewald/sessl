/**
 * *****************************************************************************
 * Copyright 2012 Roland Ewald
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
package sessl.util

import reflect._

import scala.reflect.runtime.{ universe => reflu }
import scala.collection.Seq

import sessl.MultipleVars
import sessl.Variable

/**
 * A trait to create sets of case class instances from a set of specified variables.
 *
 *  If the case class has other case classes as parameters, which are also of this type, this functionality
 *  can even be used to create nested sets of case class instances.
 *
 *  Parts of the code are adapted from or inspired by the following discussions:
 *  http://stackoverflow.com/q/4290955
 *  http://stackoverflow.com/a/4055850
 *  http://stackoverflow.com/a/7320359
 *  http://stackoverflow.com/a/4055850
 *
 *  @see sessl.Variable
 *
 *  @author Roland Ewald
 */
trait CreatableFromVariables[T <: CreatableFromVariables[T] with Product] {
  this: T =>

  /** The copy(...) method is used to create new instances. */
  private[this] val copyMethod = this.getClass.getMethods.find(x => x.getName == "copy").get

  /** The names of all specified fields. */
  lazy val fieldNames = ReflectionHelper.caseClassConstrArgs(this).map(_._1)

  /** The set of all specified field names. */
  lazy val fieldNamesSet = fieldNames.toSet

  /**
   * Create instance of class from list of parameters.
   *  @param parameters the parameter list
   */
  def scan(variablesToScan: Variable*): Seq[T] = {
    variablesToScan.filter(!_.isInstanceOf[MultipleVars]).foreach(v => require(fieldNamesSet(v.name), "No variable with name '" + v.name + "' is defined in class."))
    createSetupSet(completeSetups(Variable.createVariableSetups(variablesToScan)))
  }

  /** Gets internal parameter names and their values. */
  def getInternalParameters = {
    val renamingMap = getRenamingMap()
    (fieldNames.indices zip fieldNames).map(entry => (renamingMap.getOrElse(entry._2, entry._2), productElement(entry._1))).toMap
  }

  /** Get the map of the elements to be renamed. Should be overridden by all classes that want to rename the variables.*/
  def getRenamingMap(): Map[String, String] = Map()

  /**
   * Completes all given setups, ie sets all fixed parameters for each setup and puts all values in the right order.
   *
   *  @param setups the setups to be completed
   *  @return the completed setups
   */
  private[this] def completeSetups(setups: Seq[Map[String, Any]]) =
    for (setup <- setups) yield {
      for (i <- fieldNames.indices) yield {
        if (setup.contains(fieldNames(i)))
          setup(fieldNames(i))
        else productElement(i)
      }
    }

  /**
   * Creates the set of instances that is defined by the set of parameter lists.
   *  @param setups the set of parameter lists (these need to be valid!)
   *  @return the set of instances
   */
  private[this] def createSetupSet(setups: Seq[_ <: Seq[_]]): Seq[T] = for (setup <- setups) yield this.getCopy(setup)

  /**
   * Invokes the copy(...) method with the given list of parameters.
   *  @param parameters the correct and complete sequence of case class parameters
   *  @return a new instance of the class with these parameters
   */
  private[this] def getCopy(parameters: Seq[Any]) = {
    try {
      println("Invoking copy with params: " + parameters.mkString(","))
      copyMethod.invoke(this, parameters.map(_.asInstanceOf[AnyRef]): _*).asInstanceOf[T]
    } catch {
      case ex: IllegalArgumentException => throw new IllegalArgumentException("Have you used parentheses to specify *all* allgorithms and subalgorithms? E.g. \"algo1\" <~ Algo() etc.", ex)
    }
  }

}
