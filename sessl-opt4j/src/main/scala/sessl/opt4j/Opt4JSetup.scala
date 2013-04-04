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
import scala.Array.canBuildFrom
import org.opt4j.core.Genotype
import org.opt4j.core.Objective.Sign
import org.opt4j.core.Objectives
import org.opt4j.core.genotype.CompositeGenotype
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.genotype.IntegerGenotype
import org.opt4j.core.genotype.SelectGenotype
import org.opt4j.core.problem.Creator
import org.opt4j.core.problem.Decoder
import org.opt4j.core.problem.Evaluator
import org.opt4j.core.problem.ProblemModule
import sessl.optimization.AbstractOptimizerSetup
import sessl.optimization.BoundedSearchSpaceDimension
import sessl.optimization.GeneralSearchSpaceDimension
import sessl.optimization.SearchSpaceDimension
import sessl.optimization.SimpleParameters
import sessl.util.Logging
import sessl.util.ScalaToJava
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.viewer.ViewerModule
import org.opt4j.core.start.Opt4JTask

/**
 * Support for Opt4J.
 *
 * @author Roland Ewald
 */
class Opt4JSetup extends AbstractOptimizerSetup with Logging {

  override def execute() = {

    /**
     *  Creates random genotype.
     */
    val creator = new Creator[CompositeGenotype[String, Genotype]] {

      val rng = new Random //TODO: generalize this

      /** Composite genotype: it consists of a genotype per dimension. */
      val genotypePerDimension: Seq[(String, Genotype)] =
        for (dim <- searchSpace) yield dim match {
          case bounds @ BoundedSearchSpaceDimension(name, lower, stepSize, upper) =>
            lower match {
              case l: Double => (name, new DoubleGenotype(l, upper.asInstanceOf[Double]))
              case l: Int => (name, new IntegerGenotype(l, upper.asInstanceOf[Int]))
              case _ => throw new IllegalArgumentException("This type of numerical bound is not supported:" + lower.getClass)
            }
          case list @ GeneralSearchSpaceDimension(name, values) => (name, new SelectGenotype(ScalaToJava.toList(values)))
          case _ => throw new IllegalArgumentException("This type of search space dimension is not supported:" + dim)
        }

      override def create(): CompositeGenotype[String, Genotype] = {
        val composite = new CompositeGenotype(ScalaToJava.toMap(genotypePerDimension.toMap))
        for (paramName <- composite.keySet.toArray) {
          composite.get[Genotype](paramName) match {
            case s: SelectGenotype[_] => s.init(rng, 1)
            case d: DoubleGenotype => d.init(rng, 1)
            case i: IntegerGenotype => i.init(rng, 1)
            case x => throw new IllegalArgumentException("Genotype not supported:" + x.getClass())
          }
        }
        composite
      }
    }

    val decoder = new Decoder[CompositeGenotype[String, Genotype], SimpleParameters] {
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

    val evaluator = new Evaluator[SimpleParameters] {
      override def evaluate(params: SimpleParameters): Objectives = {
        val objectives: Objectives = new Objectives
        objectives.add("objective", Sign.MAX, objective(params))
        if (params.firstUnusedParameter >= 0)
          logger.warn("The parameter '" + params.firstUnusedParameterName.get +
            "' has not been accessed from within the objective function. Is the configuration of the search space correct?")
        objectives
      }
    }

    val problemModule = new ProblemModule() {
      override def config() {
        bindProblem(creator.getClass, decoder.getClass, evaluator.getClass)
      }
    }

    //From the tutorial:

    val ea = new EvolutionaryAlgorithmModule()
    ea.setGenerations(500)
    ea.setAlpha(100)
    val viewer = new ViewerModule()
    viewer.setCloseOnStop(true)
    val task = new Opt4JTask(false)
    task.init(ea, problemModule, viewer)
    try {
      task.execute()
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      task.close()
    }

    //    for (i <- Range(1, 10)) {
    //      val params = for (dim <- searchSpace) yield (dim.name, dim.values(Random.nextInt(dim.values.length)))
    //      println("here be opt4j dragons (using " + objective + ": " + objective(SimpleParameters(params.toMap)) + " :)")
    //    }
  }
}

object Opt4JSetup {
  type SearchSpace = Seq[SearchSpaceDimension[_]]
}

