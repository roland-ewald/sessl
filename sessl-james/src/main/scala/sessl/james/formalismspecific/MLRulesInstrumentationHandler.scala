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

import org.jamesii.model.mlrules.IMLRulesModel
import org.jamesii.core.experiments.tasks.IComputationTask
import sessl.james.SESSLInstrumenter
import org.jamesii.core.observe.IObservable
import org.jamesii.core.experiments.optimization.parameter.IResponseObserver

/**
 * Handles the instrumentation for ml-rules models.
 *
 * @author Roland Ewald
 */
class MLRulesInstrumentationHandler extends InstrumentationHandler {

  override def applicable(task: IComputationTask): Boolean = task.getModel().isInstanceOf[IMLRulesModel]

  override def configureObserver(task: IComputationTask, instrumenter: SESSLInstrumenter): IResponseObserver[_ <: IObservable] = {
    null
  }
}