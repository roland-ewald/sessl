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
package sessl.james

import sessl.Algorithm
import sessl.util.AlgorithmSet
import sessl.util.CreatableFromVariables
import java.util.logging.Level
import org.jamesii.SimSystem
import org.jamesii.core.parameters.ParameterBlock

/**
 * Utility functions to generate parameter blocks.
 * @author Roland Ewald
 *
 */
object ParamBlockGenerator {

  /** Create a set of parameter blocks from sessl algorithms. */
  def createParamBlockSet[T <: JamesIIAlgo[Factory]](algoSet: AlgorithmSet[T]): Seq[ParamBlock] =
    for (algorithm <- algoSet.algorithms) yield createParamBlock(algorithm)

  /** Creates a parameter block from a sessl algorithm specification. */
  def createParamBlock[T <: JamesIIAlgo[Factory]](algorithm: T): ParamBlock = {
    val paramBlock = new ParamBlock(algorithm.factory.getClass.getName)
    algorithm match {
      case a: CreatableFromVariables[_] =>
        a.getInternalParameters().foreach(entry => addToParamBlock(paramBlock, entry, algorithm.customBlockName(entry._1)))
      case _ =>
        report(Level.INFO, "Algorithm " + algorithm + " cannot be parameterized.")
    }
    paramBlock
  }

  /**
   * Recursively adds sub-blocks to the parameter block.
   * Basically filters out which elements are algorithms in themselves and hence need their own parameters to be set up.
   *
   * @param paramBlock
   *          the parameter block
   * @param nameAndValue
   *          the name and value to be specified
   * @param customBlockName
   * 		  if set, the custom (non-standard) name of the sub-block under which algorithm parameters are stored
   */
  private[this] def addToParamBlock(paramBlock: ParamBlock, nameAndValue: (String, Any), customBlockName: Option[String]): Unit = {
    nameAndValue._2 match {
      case algo: JamesIIAlgo[_] =>
        paramBlock.addSubBlock(customBlockName.getOrElse(Registry.getBaseFactoryFor(algo.factory.getClass).getName()),
          createParamBlock(algo))
      case pb: ParameterBlock =>
        paramBlock.addSubBl(nameAndValue._1, pb)
      case _ =>
        paramBlock.addSubBlock(nameAndValue._1, nameAndValue._2)
    }

  }

}
