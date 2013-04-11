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
package sessl.opt4j

import scala.Array.canBuildFrom

import org.opt4j.core.Genotype
import org.opt4j.core.genotype.CompositeGenotype
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.genotype.IntegerGenotype
import org.opt4j.core.genotype.SelectGenotype
import org.opt4j.core.problem.Decoder

import sessl.optimization.SimpleParameters

/**
 * Decodes genotype into phenotype.
 *
 * Here, this is simply a matter of reading out the parameter values and putting them into a map representing a parameter assignment.
 *
 * @see org.opt4j.core.problem.Decoder
 *
 * @author Roland Ewald
 */
class SimpleParameterDecoder extends Decoder[CompositeGenotype[String, Genotype], SimpleParameters] {
  override def decode(composite: CompositeGenotype[String, Genotype]): SimpleParameters = {
    val paramAssignment =
      for (paramName <- composite.keySet.toArray) yield (paramName.toString,
        composite.get[Genotype](paramName) match {
          case s: SelectGenotype[_] => s.getValue(0)
          case d: DoubleGenotype => d.get(0)
          case i: IntegerGenotype => i.get(0)
          case x => throw new IllegalArgumentException("Genotype not supported:" + x.getClass())
        })
    SimpleParameters(paramAssignment.toMap)
  }
}