package sessl
import scala.collection.mutable.ListBuffer
import sessl.util.AlgorithmSet

/** Support for configuring the simulators that shall be used.
 *  @author Roland Ewald
 *
 */
trait SupportSimulatorConfiguration {

  /** The user-defined set of simulation algorithms. */
  val simulators = AlgorithmSet[Simulator]()

  /** Defines the execution mode of the specified set of simulation algorithms. */
  var executionMode: SimulatorExecutionOption = AnySimulator

  /** Getting/setting the simulator. */
  def simulator_=(s: Simulator) = { simulators.clear(); simulators <~ Seq(s) }
  def simulator = {
    require(simulators.hasSingleElement,
      "Use simulatorSet instead of this simulator property, there are " + simulators.size + " algorithms in the set but there needs to be exactly one!")
    simulators.firstAlgorithm
  }
}

/** A type hierarchy for execution options regarding the specified algorithms. */
trait SimulatorExecutionOption

case object AnySimulator extends SimulatorExecutionOption

case object AllSimulators extends SimulatorExecutionOption
