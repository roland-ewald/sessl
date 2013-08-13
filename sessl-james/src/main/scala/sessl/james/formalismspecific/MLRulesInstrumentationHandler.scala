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
import sessl.util.Logging
import org.jamesii.model.mlrules.observation.mosan.MLRulesMosanInstrumenter
import org.jamesii.model.mlrules.observation.aggregation.SpeciesHierarchyAttributeAwareCountAggregator

/**
 * Handles the instrumentation for ml-rules models.
 *
 * @author Roland Ewald
 */
class MLRulesInstrumentationHandler extends InstrumentationHandler with Logging {

  override def applicable(task: IComputationTask): Boolean = task.getModel().isInstanceOf[IMLRulesModel]

  override def configureObservers(task: IComputationTask, instrumenter: SESSLInstrumenter, outputDir: String): Seq[IResponseObserver[_ <: IObservable]] = {

    val model = task.getModel().asInstanceOf[IMLRulesModel]
    val varsToBeObserved = instrumenter.instrConfig.varsToBeObserved
    val obsTimes = instrumenter.instrConfig.observationTimes
    val timeOfLastObservation = obsTimes.last
    val simStopTime = 1.05 * timeOfLastObservation //seems to be unused, let's set it to a 'safe' value after the last observation point

    //Some sanity checks
    require(obsTimes.size >= 2, "At least two time points need to be given; ml-rules instrumentation is based on intervals.")
    val interval = obsTimes(1) - obsTimes(0)
    require(interval > 0, "Interval should be > 0, but is:" + interval)
    val wrongInterval = obsTimes.sliding(2).find { vals => ((vals(1) - vals(0)) - interval) > 0.01 * interval } //Check for 1% deviation of observation times
    if (wrongInterval.isDefined)
      logger.warn("It seems you do not use a range of time points to observe, but ml-rules instrumentation is based on intervals. Using interval:" + interval)

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

          while (time >= nextTime && interval > 0 && nextTime <= timeOfLastObservation) {
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

    val mosanInstrumenter = new MLRulesMosanInstrumenter(outputDir: String, interval, new SpeciesHierarchyAttributeAwareCountAggregator)
    mosanInstrumenter.instrumentModel(model, task.getConfig)
    val mosanObserver = mosanInstrumenter.getInstantiatedObservers().get(0)
    //TODO: needs a wrapper, this does not hold: mosanObserver.isInstanceOf[IResponseObserver[_ <: IObservable]]    
    Mediator.create(model)
    model.registerObserver(observer)    			 
    Seq(observer) 
  }
}