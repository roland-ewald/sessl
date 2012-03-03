package sessl
import scala.collection.mutable.ListBuffer
import sessl.util.AlgorithmSet

/** Support for configuring the simulators that shall be used.
 *  @author Roland Ewald
 *
 */
trait SupportSimulatorConfiguration {

  /** The user-defined set of simulation algorithms. */
  val simulatorSet = AlgorithmSet[Simulator]()

  /** Defines the execution mode of the specified set of simulation algorithms. */
  var simulatorExecutionMode: SimulatorExecutionOption = AnySimulator

  /** Getting/setting the simulator. */
  def simulator_=(s: Simulator) = { simulatorSet.clear(); simulatorSet << Seq(s) }
  def simulator = {
//    require(simulatorSet.hasSingleElement,
//      "Use simulatorSet instead of this simulator property, there is more than one algorithm in the set!")
    simulatorSet.firstAlgorithm
  }

}

/** A type hierarchy for execution options regarding the specified algorithms. */
trait SimulatorExecutionOption

case object AnySimulator extends SimulatorExecutionOption

case object AllSimulators extends SimulatorExecutionOption
