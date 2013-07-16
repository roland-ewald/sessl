/**
 * *****************************************************************************
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
 * ****************************************************************************
 */
package sessl
import scala.collection.mutable.ListBuffer
import sessl.util.AlgorithmSet

/**
 * Support for configuring the simulators that shall be used.
 *
 *  @author Roland Ewald
 */
trait SupportSimulatorConfiguration {

  /**
   * The user-defined set of simulation algorithms.
   *
   *  @example {{{
   *    simulators <~ MyAlgorithm().scan("myAlgoParam" <~ range(1,1,10))
   *  }}}
   */
  val simulators = AlgorithmSet[Simulator]()

  /**
   * Defines the execution mode of the specified set of simulation algorithms.
   *  @example {{{
   *     executionMode = AnySimulator // Use any simulator to execute the simulation runs (default)
   *     executionMode = AllSimulators // Use all simulators, each shall execute the simulation runs
   *  }}}
   */
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
sealed trait SimulatorExecutionOption

case object AnySimulator extends SimulatorExecutionOption

case object AllSimulators extends SimulatorExecutionOption
