package sessl.sbmlsim

import org.simulator.math.odes.AbstractDESSolver
import org.simulator.math.odes.EulerMethod
import sessl.Simulator
import org.simulator.math.odes.DormandPrince54Solver
import org.simulator.math.odes.RosenbrockSolver
import sessl.util.CreatableFromVariables

/** Types to represent the algorithms offered by SBMLsimulator.
 *  @author Roland Ewald
 */
trait SBMLSimAlgorithm[+T] {
  /** Create algorithm of this type. */
  def create(): T
}

/** Super type of all ODE solvers provided by SBMLsimulator. */
trait BasicSBMLSimSimulator extends Simulator with SBMLSimAlgorithm[AbstractDESSolver] {

  /** The solver's step size. */
  def stepSize: Double

  /** Create solver and set its step size. */
  def createSolver() = {
    val solver = create()
    solver.setStepSize(stepSize)
    solver.setIncludeIntermediates(false)
    solver
  }
}

object BasicSBMLSimSimulator {
  val defaultStepSize = 1e-06
}

case class Euler(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator with CreatableFromVariables[Euler] {
  override def create() = new EulerMethod
}

case class DormandPrince54(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator with CreatableFromVariables[DormandPrince54] {
  override def create() = new DormandPrince54Solver
}

case class Rosenbrock(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator with CreatableFromVariables[Rosenbrock] {
  override def create() = new RosenbrockSolver
} 