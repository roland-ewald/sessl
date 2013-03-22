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
package sessl.james.formalismspecific

import org.jamesii.core.experiments.tasks.IComputationTask
import org.jamesii.core.observe.IObserver
import org.jamesii.core.experiments.optimization.parameter.IResponseObserver
import org.jamesii.core.observe.IObservable
import sessl.util.SimpleObservation
import sessl.james.SESSLInstrumenter

/**
 * Interface for components that handle the instrumentation for a specific formalism
 *
 * @author Roland Ewald
 */
trait InstrumentationHandler {

  /** Returns true if the handler can be applied to the given computation task. */
  def applicable(task: IComputationTask): Boolean

  /** Configure the observation of this computation task and return the observer. */
  def configureObserver(task: IComputationTask, instrumenter: SESSLInstrumenter): IResponseObserver[_ <: IObservable]

}