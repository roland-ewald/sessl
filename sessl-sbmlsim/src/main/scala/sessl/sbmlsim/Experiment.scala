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
package sessl.sbmlsim

import org.simulator.math.odes.AbstractDESSolver
import org.simulator.math.odes.DormandPrince54Solver
import org.simulator.math.odes.MultiTable
import org.simulator.sbml.SBMLinterpreter
import sessl.AbstractExperiment
import sessl.ReplicationCondition
import org.sbml.jsbml.Model
import org.sbml.jsbml.xml.stax.SBMLReader
import sessl.VariableAssignment
import sessl.StoppingCondition
import sessl.Variable
import sessl.sbmlsim._
import sessl.util.Logging

/**
 * Encapsulates the SBMLsimulator (see http://www.ra.cs.uni-tuebingen.de/software/SBMLsimulator).
 *  As only the core is provided at Sourceforge (http://sourceforge.net/projects/sbml-simulator/),
 *  this will be integrated (i.e., no functionality to set up experiments via the GUI is reused here).
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment with SBMLSimResultHandling {

  /** Describes a variable assignment (first element) and its id (second element). */
  type AssignmentDescription = (Map[String, Any], Int)

  /** Describes a job with a an id as second element and a triple (variable assignment, simulator-setup,flag-replications-done) as a first element. */
  type JobDescription = ((AssignmentDescription, BasicSBMLSimSimulator, Boolean), Int)

  /** The default solver to be used. */
  private val defaultSolver = DormandPrince54

  /** The model to be simulated. */
  private[this] var model: Option[Model] = None

  /** The solver to be used. */
  private[this] var solver: Option[BasicSBMLSimSimulator] = None

  override def basicConfiguration(): Unit = {
    configureModelLocation()
    configureSimulatorSetup()
  }

  override def replications_=(reps: Int) =
    throw new IllegalArgumentException("SBMLsimulator provides deterministic solvers, setting a number of replications is not supported.")

  override def replicationCondition_=(rc: ReplicationCondition) =
    throw new IllegalArgumentException("SBMLsimulator provides deterministic solvers, setting replication conditions is not supported.")

  override def stopCondition_=(sc: StoppingCondition) =
    throw new UnsupportedOperationException("Not implemented so far.")

  /** Configure model location. */
  def configureModelLocation() = {
    model = Some((new SBMLReader).readSBML(modelLocation.get).getModel)
    require(model.isDefined, "Reading a model from '" + modelLocation.get + "' failed.")
    logger.info("Successfully read model from " + modelLocation.get)
  }

  /** Configure simulator setup. */
  def configureSimulatorSetup() {
    require(fixedStopTime.isDefined, "No stop time is given. Use stopTime =... to set it.")
    if (simulators.isEmpty)
      simulators <+ DormandPrince54()
    simulators.algorithms.foreach(s => require(s.isInstanceOf[BasicSBMLSimSimulator], "Simulator '" + s + "' is not supported."))
  }

  /** Executes experiment*/
  def executeExperiment(): Unit = {
    //Generate all desired combinations (variable-setup, simulator)
    val jobs = for (v <- createVariableSetups().zipWithIndex; i <- simulators.algorithms.indices) yield (v, simulators.algorithms(i).asInstanceOf[BasicSBMLSimSimulator], i == simulators.size - 1)
    require(!jobs.isEmpty, "Current setup does not define any jobs to be executed.")

    //Execute all generated jobs
    executeJobs(jobs.zipWithIndex)

    //Check if the experiment went well, and finish it
    checkResultHandlingCorrectness("considerResults(...)")
    experimentDone()
  }

  /** Executes the given list of jobs. */
  def executeJobs(jobs: List[JobDescription]) = jobs.map(executeJob)

  /** Executes a job. */
  protected[sbmlsim] final def executeJob(jobDesc: JobDescription) = {

    //Retrieve IDs from assignment/job description
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    val variableAssignment = assignmentDesc._1 ++ fixedVariables
    val assignmentId = assignmentDesc._2 + 1
    val runId = jobDesc._2 + 1

    logger.info("Run #" + runId + " started, it simulates setup " + variableAssignment + " with simulator " + jobDesc._1._2)

    //Execute SBMLsimulator
    val theModel = model.get.clone()
    Experiment.assignParameters(variableAssignment, theModel)

    val interpreter = new SBMLinterpreter(theModel);
    val solution = jobDesc._1._2.createSolver().solve(interpreter, interpreter
      .getInitialValues, 0, stopTime);

    //Check if something is done with these results
    considerResults(runId, assignmentId, solution)

    //Register run execution
    addAssignmentForRun(runId, assignmentId, assignmentDesc._1.toList)
    runDone(runId)

    //Register replications execution if this is the last setup
    if (jobDesc._1._3) {
      replicationsDone(assignmentId)
    }
  }

}

object Experiment extends Logging {

  private def assignParameters(assignment: Map[String, Any], model: Model) = {
    assignment.foreach {
      varAssignment =>
        {
          val (name, value) = varAssignment
          val parameter = model.getParameter(name)
          if (parameter == null)
            logger.error("Model parameter '" + name + "' not defined. The parameters declared by the model are (in (name,value) pairs): " +
              createModelParamDesc(model).mkString(","))
          else if (!value.isInstanceOf[Number])
            logger.error("Value '" + value + "' of parameter '" + name + "' not supported, must be numeric.")
          else
            parameter.setValue(value.asInstanceOf[Number].doubleValue)
        }
    }
  }

  private def createModelParamDesc(model: Model) = {
    for (i <- 0 until model.getParameterCount()) yield (model.getParameter(i).toString, model.getParameter(i).getValue())
  }
}
