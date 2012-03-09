package sessl.sbmlsim

import org.simulator.math.odes.AbstractDESSolver
import org.simulator.math.odes.EulerMethod
import sessl.Simulator
import org.simulator.math.odes.DormandPrince54Solver
import org.simulator.math.odes.RosenbrockSolver

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
    solver
  }
}

object BasicSBMLSimSimulator {
  val defaultStepSize = 10e-05
}

case class Euler(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator {
  override def create() = new EulerMethod
}

case class DormandPrince54(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator {
  override def create() = new DormandPrince54Solver
}

case class Rosenbrock(stepSize: Double = BasicSBMLSimSimulator.defaultStepSize) extends BasicSBMLSimSimulator {
  override def create() = new RosenbrockSolver
} 