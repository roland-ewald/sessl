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
    ea.setGenerations(20)
    ea.setAlpha(10)

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

  private[this] var seed: Option[Long] = None

  def register(s: Opt4JSetup, o: Objective, f: ObjectiveFunction[_ <: Objective], se: SearchSpace, rngSeed: Long = System.currentTimeMillis): Unit =
    this.synchronized {
      require(!owner.isDefined, "There is already an owner for the setup singleton: " + owner.get)
      owner = Some(s)
      objective = Some(o)
      objectiveFunction = Some(f)
      space = Some(se)
      seed = Some(rngSeed)
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

  def createRNG() = {
    require(seed.isDefined, "No RNG seed defined.")
    new Random(seed.get)
  }
}