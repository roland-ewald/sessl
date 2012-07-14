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
package sessl.james

import james.core.experiments.taskrunner.plugintype.TaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory.NUM_CORES
import james.core.parameters.ParameterizedFactory
import simspex.adaptiverunner.AdaptiveComputationTaskRunner
import simspex.adaptiverunner.AdaptiveTaskRunnerFactory
import simspex.adaptiverunner.policies.EpsilonGreedyDecrInitFactory
import james.SimSystem
import java.util.logging.Level

import sessl._

/** Support for configuring the parallel execution in James II.
 *
 *  @author Roland Ewald
 *
 */
trait ParallelExecution extends AbstractParallelExecution {
  this: Experiment =>

  override def configureParallelExecution(numThreads: Int) = {
    val parameters = Param() :/ (NUM_CORES ~>> numThreads)
    if (simulators.size > 1) {
      SimSystem.report(Level.INFO, "Adapting the configuration of the adaptive task runner to use " + numThreads + " threads.");
      val trFactory = exp.getTaskRunnerFactory()
      require(trFactory.getFactory().getClass().isAssignableFrom(classOf[AdaptiveTaskRunnerFactory]))
      trFactory.setParameter(trFactory.getParameters() :/ (NUM_CORES ~>> numThreads))
    } else {
      exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new ParallelComputationTaskRunnerFactory, parameters))
    }
  }
}
