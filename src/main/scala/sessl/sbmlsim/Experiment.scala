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

/** Encapsulates the SBMLsimulator (see http://www.ra.cs.uni-tuebingen.de/software/SBMLsimulator).
 *  As only the core is provided at Sourceforge (http://sourceforge.net/projects/sbml-simulator/),
 *  this will be integrated (i.e., no functionality to set up experiments via the GUI is reused here).
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

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
    println("Successfully read model from " + modelLocation.get) //TODO: Use logging here
  }

  /** Configure simulator setup. */
  def configureSimulatorSetup() {
    require(fixedStopTime.isDefined, "No stop time is given. Use stopTime =... to set it.")
    if (simulatorSet.isEmpty)
      simulatorSet <+ DormandPrince54()
    simulatorSet.algorithms.foreach(s => require(s.isInstanceOf[BasicSBMLSimSimulator], "Simulator '" + s + "' is not supported."))
  }

  /** Executes experiment*/
  def execute(): Unit = {
    //Generate all desired combinations (variable-setup, simulator)
    val jobs = for (v <- createVariableSetups().zipWithIndex; i <- simulatorSet.algorithms.indices) yield (v, simulatorSet.algorithms(i).asInstanceOf[BasicSBMLSimSimulator], i == simulatorSet.size - 1)
    require(!jobs.isEmpty, "Current setup does not define any jobs to be executed.")
    executeJobs(jobs.zipWithIndex).foreach(x => println("Solution columns:" + x.getColumnCount()))
    experimentDone()
  }

  /** Creates variable setups (or list with single empty map, if none are defined). */
  def createVariableSetups(): List[Map[String, Any]] = {
    if (!variablesToScan.isEmpty)
      Variable.createMultipleVarsSetups(variablesToScan, Seq(fixedVariables)).toList
    else List(fixedVariables)
  }

  /** Executes the given list of jobs. */
  def executeJobs(jobs: List[JobDescription]) = jobs.map(executeJob)

  /** Executes a job. */
  protected[sbmlsim] def executeJob(jobDesc: JobDescription): MultiTable = {

    //Retrieve IDs from assignment/job description
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    val assignmentId = assignmentDesc._2 + 1
    val runId = jobDesc._2 + 1

    //TODO: Use logging here
    println("Run #" + runId + " started, it simulates setup " + assignmentDesc._1 + " with simulator " + jobDesc._1._2)

    //Execute SBMLsimulator
    val theModel = model.get.clone()
    Experiment.assignParameters(assignmentDesc._1, theModel)

    val interpreter = new SBMLinterpreter(theModel);
    val solution = jobDesc._1._2.createSolver().solve(interpreter, interpreter
      .getInitialValues, 0, stopTime);

    //Register run execution
    addAssignmentForRun(runId, assignmentId, assignmentDesc._1.toList)
    runDone(runId)

    //Register replications execution if this is the last setup
    if (jobDesc._1._3) {
      replicationsDone(assignmentId)
    }
    solution
  }

}

object Experiment {

  private def assignParameters(assignment: Map[String, Any], model: Model) = {
    assignment.foreach {
      varAssignment =>
        {
          val (name, value) = varAssignment
          val parameter = model.getParameter(name)
          //TODO: Use logging here
          if (parameter == null)
            println("Model parameter '" + name + "' not defined. The parameters declared by the model are (in (name,value) pairs): " +
              createModelParamDesc(model).mkString(","))
          else if (!value.isInstanceOf[Number])
            println("Value '" + value + "' of parameter '" + name + "' not supported, must be numeric.")
          else
            parameter.setValue(value.asInstanceOf[Number].doubleValue)
        }
    }
  }

  private def createModelParamDesc(model: Model) = {
    for (i <- 0 until model.getParameterCount()) yield (model.getParameter(i).toString, model.getParameter(i).getValue())
  }
}