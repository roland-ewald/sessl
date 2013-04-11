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

import java.util.Random

import org.opt4j.core.Genotype
import org.opt4j.core.genotype.CompositeGenotype
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.genotype.IntegerGenotype
import org.opt4j.core.genotype.SelectGenotype
import org.opt4j.core.problem.Creator

import sessl.optimization.BoundedSearchSpaceDimension
import sessl.optimization.GeneralSearchSpaceDimension
import sessl.util.ScalaToJava

/**
 * Represents genotype by simply creating random parameter combinations.
 * It is important that Opt4J's built-in genotypes are used, since this allows
 * to re-use the built-in operators for mutation and recombination.
 *
 * See http://opt4j.sourceforge.net/documentation/3.0/tutorial.xhtml for details.
 *
 * @see org.opt4j.core.problem.Genotype
 *
 * @author Roland Ewald
 */
class SimpleParameterCreator extends Creator[CompositeGenotype[String, Genotype]] {

  /** The source of (pseudo-)randomness to create different individuals. */
  val rng = Opt4JSetup.createRNG()

  /** Create genotype for each dimension in the search space. */
  val genotypePerDimension: Seq[(String, Genotype)] =
    for (dim <- Opt4JSetup.searchSpace) yield dim match {
      case bounds @ BoundedSearchSpaceDimension(name, lower, stepSize, upper) =>
        lower match {
          case l: Double => (name, new DoubleGenotype(l, upper.asInstanceOf[Double]))
          case l: Int => (name, new IntegerGenotype(l, upper.asInstanceOf[Int]))
          case _ => throw new IllegalArgumentException("This type of numerical bound is not supported:" + lower.getClass)
        }
      case list @ GeneralSearchSpaceDimension(name, values) => (name, new SelectGenotype(ScalaToJava.toList(values)))
      case _ => throw new IllegalArgumentException("This type of search space dimension is not supported:" + dim)
    }

  /**
   * Create a composite genotype.
   *  @return the genotype (newly sampled)
   */
  override def create(): CompositeGenotype[String, Genotype] = {
    val rv = new CompositeGenotype(ScalaToJava.toMap(genotypePerDimension.toMap))
    for (paramName <- rv.keySet.toArray) {
      rv.get[Genotype](paramName) match {
        case s: SelectGenotype[_] => s.init(rng, 1)
        case d: DoubleGenotype => d.init(rng, 1)
        case i: IntegerGenotype => i.init(rng, 1)
        case x => throw new IllegalArgumentException("Genotype not supported:" + x.getClass())
      }
    }
    rv
  }
}