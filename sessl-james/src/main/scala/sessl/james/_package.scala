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
package sessl

import java.util.logging.Level

import scala.collection.mutable.ListBuffer

import org.jamesii.SimSystem
import org.jamesii.core.experiments.tasks.ComputationTaskIDObject
import org.jamesii.core.parameters.ParameterBlock

package object james {

  /** The basic factory type.  */
  type Factory = org.jamesii.core.factories.Factory[_]

  /** The abstract factory type. */
  type AbstractFactory[T <: Factory] = org.jamesii.core.factories.AbstractFactory[T]

  /** The parameter type. */
  type ParamBlock = org.jamesii.core.parameters.ParameterBlock

  /** The parameterized factory. */
  type ParamFactory[X <: Factory] = org.jamesii.core.parameters.ParameterizedFactory[X]

  /** The stop policy factory. */
  type StopFactory = org.jamesii.core.experiments.tasks.stoppolicy.plugintype.ComputationTaskStopPolicyFactory

  /** The pair type in James II (there is a pre-defined pair type in Scala).*/
  type JamesPair[X, Y] = org.jamesii.core.util.misc.Pair[X, Y]

  /** A reference to the registry. */
  lazy val Registry = SimSystem.getRegistry();

  /**
   * Some wrappers for {@link SimSystem#report}.
   */
  //TODO: Check why not all wrappers can have the same name in package object
  //  def report(t: Throwable) = SimSystem.report(t)
  //  def report(msg: String) = SimSystem.report(Level.INFO, msg)
  def reportDetails(level: Level, msg: String, t: Throwable) = SimSystem.report(level, msg, t)
  def report(level: Level, msg: String) = SimSystem.report(level, msg)

  /**
   * Get a factory from a registry.
   * @param abstrFactoryClass the class of the abstract factory
   * @param parameters the parameters to be used
   */
  def getFactory[T <: Factory](abstrFactoryClass: java.lang.Class[_ <: AbstractFactory[T]], parameters: ParameterBlock): T =
    SimSystem.getRegistry().getFactory(abstrFactoryClass, parameters)

  /**
   * Create a new random number generator.
   * @return new RNG
   */
  def nextRNG() = SimSystem.getRNGGenerator().getNextRNG()

  /**
   * Conversion Param => ParameterBlock.
   */
  implicit def paramToParameterBlock(parameter: Param): ParameterBlock = {
    val paramBlock = new ParameterBlock(parameter.value.getOrElse(null))
    val subBlocks = for (child <- parameter.childs) yield (child._1, paramToParameterBlock(child._2))
    for (subBlock <- subBlocks)
      paramBlock.addSubBl(subBlock._1, subBlock._2)
    return paramBlock
  }

  /**
   * Conversion ParameterBlock => Param.
   */
  implicit def parameterBlockToParam(paramBlock: ParameterBlock): Param = {
    val subBlockIt = paramBlock.getSubBlocks().entrySet().iterator()
    val childs = ListBuffer[(String, Param)]()
    while (subBlockIt.hasNext()) {
      val subBlockEntry = subBlockIt.next()
      childs += ((subBlockEntry.getKey(), parameterBlockToParam(subBlockEntry.getValue())))
    }
    new Param("", if (paramBlock.getValue() == null) None else Some(paramBlock.getValue()), childs.toList.toMap)
  }

  /** Conversion from computation task IDs to run IDs. */
  implicit def compTaskIDObjToRunID(compTaskID: ComputationTaskIDObject): Int = compTaskID.toString.hashCode
}
