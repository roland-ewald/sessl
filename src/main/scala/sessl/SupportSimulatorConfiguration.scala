package sessl
import scala.collection.mutable.ListBuffer
import sessl.util.AlgorithmSet

/**
 * Support for configuring the simulators that shall be used.
 * @author Roland Ewald
 *
 */
trait SupportSimulatorConfiguration {

  /** Stored the fixed simulator setup to be used (if one is set). */
  protected[sessl] var fixedSimulator: Option[Simulator] = None

  /** The user-defined set of simulation algorithms. */
  val simulatorSet = AlgorithmSet[Simulator]()

  /** Defines the execution mode of the specified set of simulation algorithms. */
  var simulatorExecutionMode: SimulatorExecutionOption = AnySimulator

  /** Getting/setting the simulator. */
  def simulator_=(s: Simulator) = { fixedSimulator = Some(s) }
  def simulator = { fixedSimulator.get }

}

/** A type hierarchy for execution options regarding the specified algorithms. */
trait SimulatorExecutionOption

case object AnySimulator extends SimulatorExecutionOption

case object AllSimulators extends SimulatorExecutionOption
