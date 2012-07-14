/*******************************************************************************
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
 ******************************************************************************/
package sessl

import scala.collection.mutable.Map
import sessl.util.ResultOperations

/** Type hierarchy to represent results of the experiment.
 *
 *  @author Roland Ewald
 *
 */

/** Super type of all results. */
trait Result

/** The results of a single simulation run. Each experiment configuration
 *  aspect may yield own results, this class just ties them together and defines the run ID.
 *  It also has a current owner, which defines to what aspect of the run results the calls to
 *  the API are forwarded.
 *
 *  @param id
 *          the (system-specific) id of the run
 */
class RunResults(val id: Int, val assignment: VariableAssignment) extends Result with ResultAspectManagement[RunResults, RunResultsAspect]

/** The results of all runs on a specific assignment. */
class ReplicationsResults(val id: Int) extends Result with ResultAspectManagement[ReplicationsResults, ReplicationsResultsAspect] {

  /** The actual results, maps run ID (sim-system specific!) => results of the run. */
  private[this] val runs: Map[Int, RunResults] = Map()

  override def addAspect(aspect: ReplicationsResultsAspect) = {
    super.addAspect(aspect)
    aspect.setResults(getAspectsFor(runs, aspect.owner))
  }

  /** Add new run results. */
  def +=(results: RunResults) = {
    if (!runs.isEmpty)
      Result.checkAssignmentEquality(runs.get(runs.keys.toList.head).get.assignment, results.assignment)
    runs += ((results.id, results))
  }
}

/** The results of a simulation experiment. */
class ExperimentResults extends Result with ResultAspectManagement[ExperimentResults, ExperimentResultsAspect] {

  /** Maps run ID => run results. */
  private[this] val runsResults = Map[Int, RunResults]()

  /** Maps assignment ID => results of all replications. */
  private[this] val replicationsResults = Map[Int, ReplicationsResults]()

  override def addAspect(aspect: ExperimentResultsAspect) = {
    super.addAspect(aspect)
    aspect.setResults(getAspectsFor(runsResults, aspect.owner), getAspectsFor(replicationsResults, aspect.owner))
  }

  /** Add run results to an experiment. */
  def +=(results: RunResults) = {
    runsResults(results.id) = results
  }

  /** Add replication results to an experiment. */
  def +=(results: ReplicationsResults) = {
    replicationsResults(results.id) = results
  }

  /** Get the results of a specific run. */
  def forRun(runID: Int) = runsResults.get(runID).getOrElse(throw new IllegalArgumentException("Run with ID '" + runID + "' is unknown!"))

  /** Get the results of a specific assignment. */
  def forAssignment(assignID: Int) = replicationsResults.get(assignID).getOrElse(throw new IllegalArgumentException("Assignment with ID '" + assignID + "' is unknown!"))

  def addAspectForRun(runId: Int, runAspect: RunResultsAspect) = { runsResults(runId).addAspect(runAspect) }

  def addAspectForReplications(assignmentId: Int, replicationsAspect: ReplicationsResultsAspect) = { replicationsResults(assignmentId).addAspect(replicationsAspect) }
}

/** Represents a certain sub-set of experiment results.*/
trait PartialExperimentResults[T <: PartialExperimentResults[T]] extends ResultOperations {
  this: ExperimentResultsAspect =>

  /** Returns partial experiment results, which contains all runs where the assignments correspond to the specified variable values.
   *  Note that unknown variables in the given list will be ignored.
   */
  def having(variables: Variable*): T = {

    val varList =
      for (variable <- variables) yield variable match {
        case v: VarSingleVal => v
        case x => throw new IllegalArgumentException("Variable '" + x.name + "' does not specify a single value!")
      }

    val varMap = Map[String, VarSingleVal]()
    for (variable <- varList)
      varMap += ((variable.name, variable))

    def checkSelection(a: RunResultsAspect): Boolean =
      a.results.assignment.forall(variableAssignment => {
        val specifiedVariable = varMap.get(variableAssignment._1)
        !specifiedVariable.isDefined || specifiedVariable.get.value == variableAssignment._2
      })

    createPartialResult(runsResults.filter(runEntry => checkSelection(runEntry._2)), replicationsResults)
  }

  def createPartialResult(runsResults: Map[Int, RunResultsAspect], replicationsResults: Map[Int, ReplicationsResultsAspect]): T

}

/** Auxiliary methods. */
object Result {

  /** Check assignment equality.
   *
   *  @param varAssignment1
   *          the first variable assignment to be compared
   *  @param varAssignment2
   *          the second variable assignment to be compared
   */
  def checkAssignmentEquality(varAssignment1: VariableAssignment, varAssignment2: VariableAssignment): Unit = {
    require(varAssignment1 == varAssignment2, "Assignments of run results do not match: '" + varAssignment1.mkString(",") + "' vs. '" + varAssignment2.mkString(",") + "'")
  }

  /** Checks equality of IDs.
   *
   *  @param id1
   *          the first id
   *  @param id2
   *          the second id
   */
  def checkIDEquality(id1: Int, id2: Int) = {
    require(id1 == id2, "Can only merge results from the same run, but run IDs are: " + id1 + " and " + id2)
  }

  /** Clears the first map and then adds all elements from second map to it */
  private[sessl] def cleanAdd[X, Y](firstMap: Map[X, Y], secondMap: Map[X, Y]) = { firstMap.clear(); firstMap ++= secondMap }

  /** The type of the owner of result (aspects).*/
  type Owner = Class[_ <: ExperimentConfiguration]
}
