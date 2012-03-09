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

/** Encapsulates the SBMLsimulator (see http://www.ra.cs.uni-tuebingen.de/software/SBMLsimulator).
 *  As only the core is provided at Sourceforge (http://sourceforge.net/projects/sbml-simulator/),
 *  this will be integrated (i.e., no functionality to set up experiments via the GUI is reused here).
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

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
      simulatorSet << DormandPrince54()
    simulatorSet.algorithms.foreach(s => require(s.isInstanceOf[BasicSBMLSimSimulator], "Simulator '" + s + "' is not supported."))
  }

  /** Executes experiment*/
  def execute(): Unit = {
    //Analyze what variables to be scanned
    val variableSetups = Variable.createMultipleVarsSetups(variablesToScan).toList

    //Generate all desired combinations (variable-setup, simulator)
    val jobs = for (v <- variableSetups; s <- simulatorSet.algorithms) yield (v, s.asInstanceOf[BasicSBMLSimSimulator])

    //Execute all jobs
    executeJobs(jobs.zipWithIndex)
    experimentDone()
  }

  /** Executes the given list of jobs. */
  def executeJobs(jobs: List[((Map[String, Any], BasicSBMLSimSimulator), Int)]) = jobs.map(executeJob)

  /** Executes a job. */
  protected[sbmlsim] def executeJob(job: ((Map[String, Any], BasicSBMLSimSimulator), Int)): MultiTable = {

    //No replications allowed, so IDs are straightforward
    val assignmentId = job._2
    val runId = assignmentId

    //Execute SBMLsimulator
    val theModel = model.get.clone()
    //TODO: Apply parameter changes
    val interpreter = new SBMLinterpreter(theModel);
    val solution = job._1._2.createSolver().solve(interpreter, interpreter
      .getInitialValues, 0, stopTime);

    addAssignmentForRun(runId, assignmentId, job._1._1.toList)
    runDone(runId)
    replicationsDone(assignmentId)
    solution
  }
}

/** Executes a single run. */
class RunExecutor(val id: Int, val solver: BasicSBMLSimSimulator, val model: Model, val stopTime: Double) {
  val solution = {
    val interpreter = new SBMLinterpreter(model.clone())
    solver.createSolver().solve(interpreter, interpreter
      .getInitialValues, 0, stopTime);
  }
}