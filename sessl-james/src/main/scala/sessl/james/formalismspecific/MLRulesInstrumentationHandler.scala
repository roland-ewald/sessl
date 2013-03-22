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

import org.jamesii.core.base.IEntity
import org.jamesii.core.experiments.optimization.parameter.IResponseObserver
import org.jamesii.core.experiments.tasks.IComputationTask
import org.jamesii.core.model.variables.BaseVariable
import org.jamesii.core.observe.Mediator
import org.jamesii.model.mlrules.IMLRulesModel
import org.jamesii.model.mlrules.observation.TimeStepObserver
import org.jamesii.model.mlrules.observation.aggregation.SpeciesCountAggregator
import javax.transaction.NotSupportedException
import sessl.james.Experiment
import sessl.james.SESSLInstrumenter
import sessl.util.ScalaToJava
import sessl.util.SimpleObservation
import sessl.util.SimpleObserverHelper
import org.jamesii.core.observe.IObservable
import sessl.james.util.SimpleJAMESIIObserverHelper

/**
 * Handles the instrumentation for ml-rules models.
 *
 * @author Roland Ewald
 */
class MLRulesInstrumentationHandler extends InstrumentationHandler {

  override def applicable(task: IComputationTask): Boolean = task.getModel().isInstanceOf[IMLRulesModel]

  override def configureObserver(task: IComputationTask, instrumenter: SESSLInstrumenter): IResponseObserver[_ <: IObservable] = {

    val model = task.getModel().asInstanceOf[IMLRulesModel]
    Mediator.create(model)
    val obsTimes = ScalaToJava.toDoubleList(instrumenter.instrConfig.observationTimes)

    val bindings = instrumenter.instrConfig.variableBindings
    val varsToBeObserved = instrumenter.instrConfig.varsToBeObserved

    val interval = 0.01
    val simStopTime = 0.1
    val aggregator = new SpeciesCountAggregator()
    val observer = new TimeStepObserver(model, simStopTime, interval, aggregator) with SimpleJAMESIIObserverHelper[SimpleObservation] with IResponseObserver[IEntity] {

      private[this] var nextTime = getModel().getTime

      private[this] var lastTime = 0

      private[this] var execNotification = false

      configureTaskObservation(instrumenter, task)
      setConfig(instrumenter.instrConfig)

      /** Stores the data as aggregated. */
      def store(time: Double) = {
        aggregator.updateData()
        for (varToBeObserved <- sesslObsConfig.varsToBeObserved) {
          val timeAmountPair = aggregator.getCurrentData(varToBeObserved)
          addValueFor(varToBeObserved, (time, timeAmountPair.getSecondValue()))
        }
      }

      /** Could be removed once there is a single store() method of the observe to override. */
      override def handleUpdate(entity: IEntity): Unit = {
        if (entity == getModel()) {
          val time =
            if (getModel().getTime().isInfinity)
              lastTime
            else getModel().getTime()

          while (time >= nextTime && interval > 0) {
            store(nextTime)
            nextTime += interval
            execNotification = true
          }
        }
      }

      /** Could be removed once there is a single store() method of the observe to override. */
      override def executeNotification(): Boolean = {
        val ret = execNotification
        execNotification = false
        ret
      }

      override def getResponseList(): java.util.Map[String, BaseVariable[_]] = throw new NotSupportedException

    }

    model.registerObserver(observer)
    observer
  }
}