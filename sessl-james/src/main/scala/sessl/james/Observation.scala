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
package sessl.james

import java.util.HashMap
import org.jamesii.core.experiments.instrumentation.computation.IComputationInstrumenter
import org.jamesii.core.experiments.instrumentation.computation.plugintype.ComputationInstrumenterFactory
import org.jamesii.core.experiments.optimization.parameter.IResponseObserver
import org.jamesii.core.experiments.optimization.parameter.instrumenter.IResponseObsSimInstrumenter
import org.jamesii.core.experiments.tasks.IComputationTask
import org.jamesii.core.model.variables.BaseVariable
import org.jamesii.core.observe.Mediator
import org.jamesii.core.parameters.ParameterBlock
import model.sr.ISRModel
import model.sr.snapshots.SRSnapshotObserver
import sessl.util.SimpleObservation
import sessl.util.SimpleObserverHelper
import org.jamesii.core.observe.IObservable
import org.jamesii.core.parameters.ParameterizedFactory
import sessl.james.formalismspecific.SRInstrumentationHandler
import sessl.james.formalismspecific.MLRulesInstrumentationHandler

/**
 * Configuring James II for observation.
 *
 *  @author Roland Ewald
 *
 */
trait Observation extends SimpleObservation {
  this: Experiment =>

  abstract override def configure() {
    super.configure()
    if (!observationTimes.isEmpty)
      exp.setComputationInstrumenterFactory(new ParameterizedFactory[ComputationInstrumenterFactory](SESSLCompInstrFactory(this)))
  }
}

object Observation {
  val instrumentationResults = "$sessl$instrResults"
}

//The James II code for the custom instrumentation plug-in

/** Factory for the computation task instrumenter. */
case class SESSLCompInstrFactory(val instrConfig: SimpleObservation) extends ComputationInstrumenterFactory {
  override def create(parameters: ParameterBlock): IComputationInstrumenter = new SESSLInstrumenter(instrConfig)
  override def supportsParameters(parameters: ParameterBlock) = 1
}

/** The computation task instrumenter. */
class SESSLInstrumenter(val instrConfig: SimpleObservation) extends IResponseObsSimInstrumenter {

  val observers = new java.util.ArrayList[IResponseObserver[_ <: IObservable]]()

  val instrumentationHandlers = List(new SRInstrumentationHandler(), new MLRulesInstrumentationHandler())

  private[this] var myRunID: Option[Int] = None

  def setRunID(runID: Int) = {
    require(!myRunID.isDefined, "Run ID should only be set once.")
    myRunID = Some(runID)
  }

  override def getInstantiatedObservers(): java.util.List[_ <: IResponseObserver[_ <: IObservable]] = observers

  /** Copies instrumented data into response, which can be processed by experiment steerers, like optimization algorithms. */
  override def getObservedResponses(): java.util.Map[String, _ <: BaseVariable[_]] = {
    require(myRunID.isDefined, "The run ID should be defined...")
    val resultMap: java.util.Map[String, BaseVariable[_]] = new HashMap()
    val baseVar = new BaseVariable[Any](Observation.instrumentationResults)
    baseVar.setValue(instrConfig.collectResults(myRunID.get))
    resultMap.put(baseVar.getName, baseVar)
    resultMap
  }

  /** Creates dedicated, formalism-specific observer and configures it to additionally record the desired variables.*/
  override def instrumentComputation(computation: IComputationTask): Unit = {
    observers.clear
    val handler = instrumentationHandlers.find(h => h.applicable(computation))
    require(handler.isDefined, "Instrumentation of this kind of model " + computation.getModel() + "is not yet supported so far!")
    //TODO: Manage parameters explicitly: computation.getConfig().getParameters()
    val observer = handler.get.configureObserver(computation, this)
    observers.add(observer)
  }
}
