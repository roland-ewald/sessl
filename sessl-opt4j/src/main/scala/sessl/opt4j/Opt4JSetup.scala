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

import scala.collection.mutable.MapBuilder
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.genotype.IntegerGenotype
import org.opt4j.core.genotype.PermutationGenotype
import sessl.optimization.AbstractOptimizerSetup
import sessl.optimization.BoundedSearchSpaceDimension
import sessl.optimization.GeneralSearchSpaceDimension
import sessl.optimization.SimpleParameters
import sessl.util.ScalaToJava
import sessl.util.Logging

/**
 * Support for Opt4J.
 *
 * @author Roland Ewald
 */
class Opt4JSetup extends AbstractOptimizerSetup with Logging {

  override def execute() = {

    //Construct genotype
    for (dim <- searchSpace) yield {
      dim match {
        case bounds @ BoundedSearchSpaceDimension(name, lower, upper) =>
          lower match {
            case l: Double => new DoubleGenotype(l, upper.asInstanceOf[Double])
            case l: Int => new IntegerGenotype(l, upper.asInstanceOf[Int])
            case _ => throw new IllegalArgumentException("This type of numerical bound is not supported:" + lower.getClass)
          }
        case list @ GeneralSearchSpaceDimension(name, values) => new PermutationGenotype(ScalaToJava.toList(values))
        case _ => throw new IllegalArgumentException("This type of search space dimensions is not supported:" + dim)
      }
    }

    //Won't work:
    val params = SimpleParameters(Map())
    objective(params)

    if (params.firstUnusedParameter >= 0)
      logger.warn("The parameter '" + params.firstUnusedParameterName.get +
        "' has not been accessed from within the objective function. Is the configuration of the search space correct?")

    //    for (i <- Range(1, 10)) {
    //      val params = for (dim <- searchSpace) yield (dim.name, dim.values(Random.nextInt(dim.values.length)))
    //      println("here be opt4j dragons (using " + objective + ": " + objective(SimpleParameters(params.toMap)) + " :)")
    //    }
  }
}