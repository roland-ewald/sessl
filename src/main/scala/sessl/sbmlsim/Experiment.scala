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

/** Encapsulates the SBMLsimulator (see http://www.ra.cs.uni-tuebingen.de/software/SBMLsimulator).
 *  As only the core is provided at sourceforge (http://sourceforge.net/projects/sbml-simulator/), this will be integrated (i.e., no
 *  functionality to set up experiments via the GUI is reused here).
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

  /** The model to be simulated. */
  private[this] var model: Option[Model] = None

  /** The solver to be used. */
  private[this] var solver: Option[AbstractDESSolver] = None

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

  //TODO: implement 'scan'?
  //TODO: implement parallel execution with actors?

  /** Configure model location. */
  def configureModelLocation() = {
    model = Some((new SBMLReader).readSBML(modelLocation.get).getModel)
    require(model.isDefined, "Reading a model from '" + modelLocation.get + "' failed.")
    println("Successfully read model from " + modelLocation.get) //TODO: Use logging here
  }

  /** Configure simulator setup. */
  def configureSimulatorSetup() {
    require(simulatorSet.size <= 1, "Usage of multiple simulator not supported.")
    solver = Some(new DormandPrince54Solver)
    //TODO: 
    solver.get.setStepSize(10e-05)
    solver.get.setIncludeIntermediates(false)
  }

  def execute(): Unit = {
    val interpreter = new SBMLinterpreter(model.get);
    val solution = solver.get.solve(interpreter, interpreter
      .getInitialValues, 0, fixedStopTime.get);

    println("Solution columns:" + solution.getColumnCount)
    val runId = 1
    val assignmentId = 1
    addAssignmentForRun(1, 1, List(("nothing", 42)))
    runDone(runId)
    replicationsDone(assignmentId)
    experimentDone()
  }

}