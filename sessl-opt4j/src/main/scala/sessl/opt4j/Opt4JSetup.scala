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
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.problem.Creator
import org.opt4j.core.problem.Decoder
import org.opt4j.core.problem.Evaluator
import org.opt4j.core.problem.ProblemModule
import org.opt4j.core.start.Opt4JTask
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.viewer.ViewerModule

import sessl.optimization.AbstractOptimizerSetup
import sessl.optimization.BoundedSearchSpaceDimension
import sessl.optimization.GeneralSearchSpaceDimension
import sessl.optimization.MultiObjective
import sessl.optimization.Objective
import sessl.optimization.ObjectiveFunction
import sessl.optimization.OptDirection
import sessl.optimization.SearchSpaceDimension
import sessl.optimization.SimpleParameters
import sessl.optimization.SingleObjective
import sessl.util.Logging
import sessl.util.ScalaToJava

/**
 * Support for Opt4J.
 *
 * @author Roland Ewald
 */
class Opt4JSetup extends AbstractOptimizerSetup with Logging {

  /** Flag to control whether the GUI of Opt4J is shown during optimization or not. */
  var showViewer: Boolean = false

  val problemModule = new ProblemModule() {
    override def config() {
      bindProblem(
        classOf[SimpleParameterCreator],
        classOf[SimpleParameterDecoder],
        classOf[SimpleParameterEvaluator])
    }
  }

  override def execute() = {

    Opt4JSetup.register(this, objective, objectiveFunction, searchSpace)

    //Termination criterion -- TODO: move to case classes
    val ea = new EvolutionaryAlgorithmModule()
    ea.setGenerations(2)
    ea.setAlpha(5)

    //Initialize task
    val task = new Opt4JTask(false)
    if (showViewer)
      task.init(ea, problemModule, new ViewerModule)
    else
      task.init(ea, problemModule)

    try {
      task.execute()
      val archive = task.getInstance(classOf[Archive])
      val it = archive.iterator
      while (it.hasNext) {
        println("OPT:")
        val ind = it.next.getPhenotype()
        println(ind.toString)
        //TODO: withOptResults(...)
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      task.close()
      Opt4JSetup.release(this)
    }
  }
}

/**
 *
 */
object Opt4JSetup {

  type SESSLObjectiveFun[-X <: Objective] = sessl.optimization.ObjectiveFunction[X]

  type SearchSpace = Seq[SearchSpaceDimension[_]]

  private[this] var owner: Option[Opt4JSetup] = None

  private[this] var objectiveFunction: Option[SESSLObjectiveFun[_]] = None

  private[this] var objective: Option[Objective] = None

  private[this] var space: Option[SearchSpace] = None

  def register(s: Opt4JSetup, o: Objective, f: ObjectiveFunction[_ <: Objective], se: SearchSpace): Unit =
    this.synchronized {
      require(!owner.isDefined, "There is already an owner for the setup singleton: " + owner.get)
      owner = Some(s)
      objective = Some(o)
      objectiveFunction = Some(f)
      space = Some(se)
    }

  def searchSpace = {
    require(space.isDefined, "No search space defined.")
    space.get.toList
  }

  def eval(params: SimpleParameters, o: Objective) =
    this.synchronized {
      require(objectiveFunction.isDefined, "No objective function defined.")
      objectiveFunction.get.asInstanceOf[SESSLObjectiveFun[Objective]](params, o)
    }

  def copyObjective(): Objective = {
    require(objective.isDefined, "No objective defined.")
    Objective.copy(objective.get)
  }

  def release(s: Opt4JSetup): Unit = this.synchronized {
    require(owner.isDefined && owner.get.eq(s), "Not the owner of the setup singleton: " + s)
    owner = None
    objectiveFunction = None
    objective = None
    space = None
  }
}

/**
 *  Creates random genotype.
 */
class SimpleParameterCreator extends Creator[CompositeGenotype[String, Genotype]] {

  val rng = new Random //TODO: generalize this

  /** Composite genotype: it consists of a genotype per dimension. */
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

/**
 * Decodes genotype into phenotype.
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

/**
 * Evaluates phenotype.
 */
class SimpleParameterEvaluator extends Evaluator[SimpleParameters] with Logging {

  implicit def optDirectionToSign(d: OptDirection) = if (d == sessl.optimization.min) Sign.MIN else Sign.MAX

  override def evaluate(params: SimpleParameters): Objectives = {
    val objectives: Objectives = new Objectives

    val newObjective = Opt4JSetup.copyObjective()
    Opt4JSetup.eval(params, newObjective) // TODO: support multi-objectives

    newObjective match {
      case obj: SingleObjective => objectives.add("objective", obj.direction, obj.singleValue)
      case obj: MultiObjective => println("DONE!")
    }

    if (params.firstUnusedParameter >= 0)
      logger.warn("The parameter '" + params.firstUnusedParameterName.get +
        "' has not been accessed from within the objective function. Is the configuration of the search space correct?")
    objectives
  }
}