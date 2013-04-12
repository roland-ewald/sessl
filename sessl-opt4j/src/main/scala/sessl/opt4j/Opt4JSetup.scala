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
import org.opt4j.core.optimizer.Archive
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
import java.util.Random

/**
 * Support for Opt4J.
 *
 * @author Roland Ewald
 */
class Opt4JSetup extends AbstractOptimizerSetup with Logging {

  /** Flag to control whether the GUI of Opt4J is shown during optimization or not. */
  var showViewer: Boolean = false

  /** Defines the Opt4J problem module to be used. */
  val problemModule = new ProblemModule() {
    override def config() {
      bindProblem(
        classOf[SimpleParameterCreator],
        classOf[SimpleParameterDecoder],
        classOf[SimpleParameterEvaluator])
    }
  }

  //Termination criterion -- TODO: move to case classes
  val ea = new EvolutionaryAlgorithmModule()
  ea.setGenerations(3)
  ea.setAlpha(10)

  override def execute() = {
    Opt4JSetup.register(this, objective, objectiveFunction, searchSpace)
    val task = initializeOptimizationTask()

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

  private[this] def initializeOptimizationTask() = {
    val rv = new Opt4JTask(false)
    if (showViewer)
      rv.init(ea, problemModule, new ViewerModule)
    else
      rv.init(ea, problemModule)
    rv
  }
}

/**
 * Companion object for the setup. Since Opt4J uses the GUICE framework for dependency injection,
 * which does not seen to support the binding of anonymous inner classes, this object contains all
 * problem-specific information that is required by the creator/decoder/evaluator classes.
 *
 * To avoid bugs (race conditions etc.), the setup has a dedicated owner, i.e. in the current implementation
 * Opt4J cannot be used to optimize multiple problems in parallel, only sequentially (this should not be too big a deal, as
 * each objective function evaluation may be parallelized, and also the evaluation of different individuals).
 *
 * @see SimpleParameterCreator
 * @see SimpleParameterDecoder
 * @see SimpleParameterEvaluator
 */
object Opt4JSetup {

  /** The objective function of SESSL (there is a name clash with Opt4J's objective function).*/
  type SESSLObjectiveFun[-X <: Objective] = sessl.optimization.ObjectiveFunction[X]

  /** The search space is defined as a sequence of search space dimensions. */
  type SearchSpace = Seq[SearchSpaceDimension[_]]

  /** The current owner of this setup. */
  private[this] var owner: Option[Opt4JSetup] = None

  /** The objective function. */
  private[this] var objectiveFunction: Option[SESSLObjectiveFun[_]] = None

  /** A sample container for objective values. */
  private[this] var objective: Option[Objective] = None

  /** The space to be searched. */
  private[this] var space: Option[SearchSpace] = None

  /** The RNG seed to be used. */
  private[this] var seed: Option[Long] = None

  /**
   * Register the given setup as owner, store the given problem-specific information.
   * @param s the setup (new owner)
   * @param o objective
   * @param f objective function
   * @param se the search space
   * @param rngSeed the RNG seed (System#currentTimeMillis per default)
   */
  def register(s: Opt4JSetup, o: Objective, f: ObjectiveFunction[_ <: Objective], se: SearchSpace, rngSeed: Long = System.currentTimeMillis): Unit =
    this.synchronized {
      require(!owner.isDefined, "There is already an owner for the setup singleton: " + owner.get)
      owner = Some(s)
      objective = Some(o)
      objectiveFunction = Some(f)
      space = Some(se)
      seed = Some(rngSeed)
    }

  /**
   * Get the search space.
   *  @return the search space
   */
  def searchSpace =
    this.synchronized {
      require(space.isDefined, "No search space defined.")
      space.get.toList
    }

  /**
   * Evaluate the current objective function for the given parameters.
   *  @param params the input parameters of the objective function
   *  @return the results
   */
  def eval(params: SimpleParameters): Objective =
    this.synchronized {
      require(objectiveFunction.isDefined, "No objective function defined.")
      val rv = copyObjective()
      objectiveFunction.get.asInstanceOf[SESSLObjectiveFun[Objective]](params, rv)
      rv
    }

  /** Releases ownership of this object. */
  def release(s: Opt4JSetup): Unit =
    this.synchronized {
      require(owner.isDefined && owner.get.eq(s), "Not the owner of the setup singleton: " + s)
      owner = None
      objectiveFunction = None
      objective = None
      space = None
      seed = None
    }

  /** Creates (pseudo) random number generator with a fixed seed (defined by the owner).*/
  def createRNG() =
    this.synchronized {
      require(seed.isDefined, "No RNG seed defined.")
      new Random(seed.get)
    }

  /**
   * Creates a new copy of an objective (values container, @see Objective).
   *  @return copy of objective value container
   */
  private[this] def copyObjective(): Objective = {
    require(objective.isDefined, "No objective defined.")
    Objective.copy(objective.get)
  }
}