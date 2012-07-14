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

/**
 * Trait to create an event log file for each simulation run, which contains each *single* event from the execution.
 *
 * In contrast to {@link sessl.AbstractObservation}, the (potentially huge amount of) data is not held in memory and cannot be further process in SESSL.
 *
 * @author Roland Ewald
 *
 */
abstract trait AbtractEventLogRecording extends ExperimentConfiguration {
  override def configure = {
    super.configure()
    configureEventLogRecording()
  }

  /** Configure event log recording. */
  def configureEventLogRecording(): Unit
}
