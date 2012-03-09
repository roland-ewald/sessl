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
      simulatorSet <+ DormandPrince54()
    simulatorSet.algorithms.foreach(s => require(s.isInstanceOf[BasicSBMLSimSimulator], "Simulator '" + s + "' is not supported."))
  }

  /** Executes experiment*/
  def execute(): Unit = {
    //Generate all desired combinations (variable-setup, simulator)
    val jobs = for (v <- createVariableSetups(); s <- simulatorSet.algorithms) yield (v, s.asInstanceOf[BasicSBMLSimSimulator])
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
  def executeJobs(jobs: List[((Map[String, Any], BasicSBMLSimSimulator), Int)]) = jobs.map(executeJob)

  /** Executes a job. */
  protected[sbmlsim] def executeJob(job: ((Map[String, Any], BasicSBMLSimSimulator), Int)): MultiTable = {

    //No replications allowed, so IDs are straightforward (just correct for indices starting with 0)
    val assignmentId = job._2 + 1
    val runId = assignmentId
    println("Run #" + runId + " started, it simulates setup " + job._1._1 + " with simulator " + job._1._2) //TODO: Use logging here

    //Execute SBMLsimulator
    val theModel = model.get.clone()
    //TODO: Apply parameter changes
    val interpreter = new SBMLinterpreter(theModel);
    val solution = job._1._2.createSolver().solve(interpreter, interpreter
      .getInitialValues, 0, stopTime);

    addAssignmentForRun(runId, assignmentId, job._1._1.toList)
    runDone(runId)
    replicationsDone(assignmentId)
    println("Run done with id:" + runId) //TODO: Use logging here
    solution
  }
}