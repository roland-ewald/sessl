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
import james.core.experiments.IExperimentExecutionListener
import james.core.experiments.BaseExperiment
import james.core.experiments.taskrunner.ITaskRunner
import james.core.experiments.ComputationTaskRuntimeInformation

/**
 * An adapter for experiment execution listeners.
 *
 * @author Roland Ewald
 */
class ExperimentExecutionAdapter extends IExperimentExecutionListener {

  override def experimentExecutionStarted(exp: BaseExperiment): Unit = {}

  override def experimentExecutionStopped(exp: BaseExperiment): Unit = {}

  override def simulationExecuted(taskRunner: ITaskRunner,
    crti: ComputationTaskRuntimeInformation, jobDone: Boolean): Unit = {}

  override def simulationInitialized(taskRunner: ITaskRunner,
    crti: ComputationTaskRuntimeInformation): Unit = {}

}
