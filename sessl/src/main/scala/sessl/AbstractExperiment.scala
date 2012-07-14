/*******************************************************************************
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
 ******************************************************************************/
package sessl

import scala.collection.mutable.ListBuffer
import sessl.util.MiscUtils
import sessl.util.ExperimentObserver
import sessl.util.Logging

/**
 * Super class for all fundamental experiment classes.
 *  It handles the execution of the event handlers and checks whether other traits that may be mixed in by the user have properly called their super methods.
 *  @author Roland Ewald
 */
abstract class AbstractExperiment extends BasicExperimentConfiguration with SupportModelConfiguration
  with SupportSimulatorConfiguration with SupportRNGSetup with SupportReplicationConditions with SupportStoppingConditions {

  /**
   * This flag checks whether the stacked configuration traits have properly called their super methods.
   *  This is possible because the experiment object itself is at the end of this line.
   */
  private[this] var configureCalled = false

  /**
   * Abstract method to create the basic setup (as configure is already used for checking whether the traits properly called super.configure()).
   *  In this function, the experiment should be initialized to conform to all elements provided in this class.
   */
  protected def basicConfiguration(): Unit

  /** Called to execute the experiment. */
  protected[sessl] def executeExperiment(): Unit

  /** Can be overridden to free resources. */
  protected[sessl] def finishExperiment(): Unit = {}

  /** Setting the flags that control a proper call hierarchy, calling event handlers if installed. */
  override def configure() = { configureCalled = true }

  /** Prepares the experiment for execution. */
  private final def prepare() = {
    require(modelLocation.isDefined, "No model is given. Use model = ... to set one.")
    basicConfiguration()
    configure()
    require(configureCalled, "Configuration incomplete: one of the mixed-in traits does not implement stacking properly (i.e., super.configure() is not called!).")
  }

}

/** Methods that operate on the specified experiments. */
object AbstractExperiment {

  /**
   * Execute experiments sequentially.
   *
   *  @param experiments
   *            the experiments
   */
  def execute(exps: AbstractExperiment*) = for (exp <- exps) {
    try {
      exp.prepare()
      exp.executeExperiment()
      require(exp.isDone, "The experiment seems to have finished incomplete.")
    } finally {
      exp.finishExperiment()
    }
  }

  //Define additional methods for doing things with experiments here...
}
