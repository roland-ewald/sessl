/**
 * *****************************************************************************
 * Copyright 2013 Roland Ewald
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
package sessl.james.util

import sessl.util.SimpleObservation
import sessl.util.SimpleObserverHelper
import org.jamesii.core.experiments.tasks.IComputationTask
import sessl.james.Experiment
import sessl.james.SESSLInstrumenter

/**
 * Simple observer helper that also provides an auxiliary method that is JAMESII-specific.
 *
 * @author Roland Ewald
 */
trait SimpleJAMESIIObserverHelper[I <: SimpleObservation] extends SimpleObserverHelper[I] {

  /** Retrieve necessary information on the current setup from computation task, configure instrumenter and SESSL data store accordingly.*/
  def configureTaskObservation(instrumenter: SESSLInstrumenter, task: IComputationTask) = {
    val runID = sessl.james.compTaskIDObjToRunID(task.getUniqueIdentifier)
    val configSetup = Experiment.taskConfigToAssignment(task.getConfig())
    setRunID(runID)
    instrumenter.setRunID(runID)
    setAssignmentID(configSetup._1)
    setAssignment(configSetup._2)
  }

}