package sessl.james

import java.util.ArrayList
import java.util.HashMap
import scala.collection.mutable.ListBuffer
import james.core.experiments.instrumentation.computation.plugintype.ComputationInstrumenterFactory
import james.core.experiments.instrumentation.computation.IComputationInstrumenter
import james.core.experiments.optimization.parameter.instrumenter.IResponseObsSimInstrumenter
import james.core.experiments.optimization.parameter.IResponseObserver
import james.core.experiments.tasks.IComputationTask
import james.core.experiments.IComputationTaskConfiguration
import james.core.experiments.SimulationRunConfiguration
import james.core.model.variables.BaseVariable
import james.core.model.IModel
import james.core.observe.Mediator
import james.core.parameters.ParameterBlock
import model.sr.snapshots.SRSnapshotObserver
import model.sr.ISRModel
import sessl.util.ObserverHelper
import sessl.util.SimpleObserverHelper
import sessl.AbstractObservation
import sessl.RunResults
import sessl.VariableAssignment
import sessl.util.SimpleObservation

/** Configuring James II for observation.
 *
 *  @author Roland Ewald
 *
 */
trait Observation extends SimpleObservation {
  this: Experiment =>

  abstract override def configure() {
    super.configure()
    if (!observationTimes.isEmpty)
      exp.setComputationInstrumenterFactory(SESSLCompInstrFactory(this))
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

  val observers = new java.util.ArrayList[IResponseObserver]()

  private[this] var myRunID: Option[Int] = None

  override def getInstantiatedObservers(): java.util.List[_ <: IResponseObserver] = observers

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

    //TODO: This is currently FORMALISM-SPECIFIC (should be replaced by general instrumentation mechanism)
    require(computation.getProcessorInfo().getLocal().getModel().isInstanceOf[ISRModel], "Only SR models are supported so far!")
    
    val model = computation.getProcessorInfo().getLocal().getModel().asInstanceOf[ISRModel]

    computation.getConfig().getParameters() //TODO: Manage parameters explicitly!

    Mediator.create(model)
    val obsTimes = new Array[java.lang.Double](instrConfig.observationTimes.length)
    for (i <- obsTimes.indices)
      obsTimes(i) = instrConfig.observationTimes(i)

    val bindings = instrConfig.variableBindings
    val varsToBeObserved = instrConfig.varsToBeObserved

    val observer = new SRSnapshotObserver(obsTimes, 1000) with SimpleObserverHelper[SimpleObservation] {

      registerCompTask(computation)

      val configSetup = Experiment.taskConfigToAssignment(computation.getConfig())
      setAssignmentID(configSetup._1)
      setAssignment(configSetup._2)
      setConfig(instrConfig)

      /** If the SR snapshot observer decides to store the data, we have to collect it too.*/
      override def store() = {
        val state = model.getState()
        val time = model.getTime()
        val speciesMap = model.getObjectMapping()
        super.store()
        for (varToBeObserved <- sesslInstrConfig.varsToBeObserved) {
          val amount = state.get(speciesMap.get(varToBeObserved))
          addValueFor(varToBeObserved, (time, amount))
        }
      }

      private[this] def registerCompTask(computation: IComputationTask) = {
        val runID = compTaskIDObjToRunID(computation.getUniqueIdentifier)
        setRunID(runID)
        require(!myRunID.isDefined, "Run ID should only be set once.")
        myRunID = Some(runID)
      }
    }
    //FORMALISM-SPECIFIC part ends

    model.registerObserver(observer)
    observers.add(observer)
  }
}
