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
import scala.collection.mutable.ListBuffer
import org.opt4j.core.Individual
import org.opt4j.core.Objective.Sign
import org.opt4j.core.Objectives
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.optimizer.Population
import org.opt4j.core.problem.ProblemModule
import org.opt4j.core.start.Opt4JTask
import org.opt4j.viewer.ViewerModule
import sessl.optimization.AbstractOptimizerSetup
import sessl.optimization.MultipleSolutionsAction
import sessl.optimization.Objective
import sessl.optimization.ObjectiveFunction
import sessl.optimization.OptDirection
import sessl.optimization.OptimizationParameters
import sessl.optimization.SearchSpaceDimension
import sessl.optimization.SimpleParameters
import sessl.optimization._
import sessl.util.Logging
import org.opt4j.core.common.completer.IndividualCompleterModule
import org.opt4j.core.start.Opt4JModule
import sessl.util.ParallelExecutionConfiguration

/**
 * Support for Opt4J.
 *
 * @author Roland Ewald
 */
class Opt4JSetup extends AbstractOptimizerSetup with Logging {

  /** Flag to control whether the GUI of Opt4J is shown during optimization or not. */
  var showViewer: Boolean = false

  /**
   * Controls how many parallel threads to be used. 0 means to exploit all available cores (n_cores),
   *  negative numbers x mean that n_cores - |x| threads should be started.
   */
  var parallelThreads: Int = 1

  /** Specifies which optimization algorithm to use. */
  protected[opt4j] var optAlgorithm: Option[Opt4JAlgorithm] = None

  /** Getting/setting the optimization algorithm. */
  def optimizer_=(a: Opt4JAlgorithm) = { optAlgorithm = Some(a) }
  def optimizer = optAlgorithm.get

  /** Defines the Opt4J problem module to be used. */
  val problemModule = new ProblemModule {
    override def config() {
      bindProblem(
        classOf[SimpleParameterCreator],
        classOf[SimpleParameterDecoder],
        classOf[SimpleParameterEvaluator])
      addOptimizerIterationListener(classOf[IterationListener])
    }
  }

  override def execute() = {
    require(optAlgorithm.isDefined, "No optimization algorithm is defined. Use, for example, \"optimizer = EvoluationaryAlgorithm\"")
    Opt4JSetup.register(this, objective, objectiveFunction, searchSpace)

    val task = initializeOptimizationTask()

    try {
      task.execute()
      val archive = task.getInstance(classOf[Archive])
      val population = task.getInstance(classOf[Population])
      callEventHandlers(afterOptimizationActions, population.iterator)
      callEventHandlers(optimizationResultActions, archive.iterator)
    } catch {
      case e: Exception => logger.error("Optimization failed", e)
    } finally {
      task.close()
      Opt4JSetup.release(this)
    }
  }

  /**
   * Calls event handlers with a list of (converted) solutions to the optimization problem (parameters + objective values).
   *  @param handlers the list of event handlers to be called
   *  @param it the iterator to access the individuals that shall be handed over
   */
  private def callEventHandlers(handlers: List[MultipleSolutionsAction], it: java.util.Iterator[Individual]): Unit =
    if (!handlers.isEmpty) {
      val convertedResults = convertResults(it)
      handlers.foreach(_(convertedResults))
    }

  /**
   * Converts the result data from Opt4J-specific data structures to the one expected by event handlers.
   * @param it iterator over the individuals to be handled
   * @return input data for event handlers
   */
  private[this] def convertResults(it: java.util.Iterator[Individual]): List[(OptimizationParameters, Objective)] = {

    /** All data to reconstruct the results (and the goals) of the optimization (results refer to a specific parameter setup).*/
    type ObjectiveData = (Map[String, OptDirection], Map[String, Double])

    /** Extract relevant data from Opt4J's Objectives. */
    def extractDataFromObjectives(os: Objectives): ObjectiveData = {
      val it = os.getKeys.iterator()
      val dirs = ListBuffer[(String, OptDirection)]()
      val vals = ListBuffer[(String, Double)]()
      while (it.hasNext) {
        val o = it.next
        dirs += ((o.getName, Opt4JSetup.signToOptDir(o.getSign)))
        vals += ((o.getName, os.get(o).getDouble().toDouble))
      }
      (dirs.toMap, vals.toMap)
    }

    /** Create a SESSL objective from the data*/
    def createObjective(d: ObjectiveData): Objective = {
      if (d._1.size == 1)
        SingleObjective(d._1.values.head, d._2.values.head)
      else
        MultiObjective(d._1, d._2)
    }

    val rv = ListBuffer[(OptimizationParameters, Objective)]()
    while (it.hasNext) {
      val individual = it.next
      val data = extractDataFromObjectives(individual.getObjectives())
      rv += ((individual.getPhenotype().asInstanceOf[SimpleParameters], createObjective(data)))
    }
    rv.toList
  }

  private[this] def initializeOptimizationTask() = {

    val modules = ListBuffer[Opt4JModule]()
    modules += optAlgorithm.get.create
    modules += problemModule

    if (showViewer)
      modules += new ViewerModule

    if (parallelThreads != 1) {
      val comp = new IndividualCompleterModule();
      comp.setType(IndividualCompleterModule.Type.PARALLEL);
      val threadConfig = ParallelExecutionConfiguration.calculateNumberOfThreads(parallelThreads)
      threadConfig._2.map(logger.warn(_))
      comp.setThreads(threadConfig._1)
      modules += comp
    }

    val rv = new Opt4JTask(false)
    rv.init(modules: _*)
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
 *
 * @see AbstractOptimizerSetup
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
  def eval(params: SimpleParameters): Objective = {
    require(objectiveFunction.isDefined, "No objective function defined.")
    val rv = copyObjective()
    //Execution:
    objectiveFunction.get.asInstanceOf[SESSLObjectiveFun[Objective]](params, rv)
    //Event handling:
    owner.get.afterEvaluationActions.foreach(_.apply(params, rv))
    rv
  }

  /**
   * Releases ownership of this object.
   *  @param s the setup that currently owns the singleton
   */
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
   * Event handler to be called by the {@link IterationListener} whenever an iteration is done.
   * @see IterationListener
   * @param population the population
   * @param archive the archive containing the best individuals (pareto front)
   */
  protected[opt4j] def iterationComplete(population: Population, archive: Archive) = owner.map { o =>
    o.callEventHandlers(o.afterIterationActions, population.iterator)
    o.callEventHandlers(o.iterationResultActions, archive.iterator)
  }

  /**
   * Creates a new copy of an objective (values container, @see Objective).
   *  @return copy of objective value container
   */
  private[this] def copyObjective(): Objective = {
    require(objective.isDefined, "No objective defined.")
    Objective.copy(objective.get)
  }

  /** Convert optimization direction into sign (used by Opt4J to distinguish this).*/
  def optDirToSign(d: OptDirection) = if (d == sessl.optimization.min) Sign.MIN else Sign.MAX

  /** Convert optimization direction into sign (used by Opt4J to distinguish this).*/
  def signToOptDir(s: Sign) = if (s == Sign.MIN) sessl.optimization.min else sessl.optimization.max
}