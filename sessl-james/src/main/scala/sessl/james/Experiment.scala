/**
 * *****************************************************************************
 *  Copyright 2012 Roland Ewald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ****************************************************************************
 */
package sessl.james

import java.net.URI
import java.util.logging.Level
import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import org.jamesii.core.experiments.replication.ConfidenceIntervalCriterion
import org.jamesii.core.experiments.replication.IReplicationCriterion
import org.jamesii.core.experiments.replication.ReplicationNumberCriterion
import org.jamesii.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory
import org.jamesii.core.experiments.taskrunner.plugintype.TaskRunnerFactory
import org.jamesii.core.experiments.taskrunner.ITaskRunner
import org.jamesii.core.experiments.tasks.stoppolicy.plugintype.ComputationTaskStopPolicyFactory
import org.jamesii.core.experiments.tasks.stoppolicy.steps.StepCountStopFactory
import org.jamesii.core.experiments.tasks.stoppolicy.EmptyStopConditionStopPolicyFactory
import org.jamesii.core.experiments.variables.modifier.IVariableModifier
import org.jamesii.core.experiments.variables.modifier.IncrementModifierDouble
import org.jamesii.core.experiments.variables.modifier.IncrementModifierInteger
import org.jamesii.core.experiments.variables.modifier.SequenceModifier
import org.jamesii.core.experiments.variables.ExperimentVariable
import org.jamesii.core.experiments.BaseExperiment
import org.jamesii.core.experiments.ComputationTaskRuntimeInformation
import org.jamesii.core.experiments.IComputationTaskConfiguration
import org.jamesii.core.experiments.RunInformation
import org.jamesii.core.experiments.SimulationRunConfiguration
import org.jamesii.core.math.random.generators.plugintype.RandomGeneratorFactory
import org.jamesii.core.model.variables.BaseVariable
import org.jamesii.core.parameters.ParameterizedFactory
import org.jamesii.core.processor.plugintype.ProcessorFactory
import org.jamesii.core.simulationrun.stoppolicy.CompositeCompTaskStopPolicyFactory
import org.jamesii.core.simulationrun.stoppolicy.ConjunctiveSimRunStopPolicyFactory
import org.jamesii.core.simulationrun.stoppolicy.DisjunctiveSimRunStopPolicyFactory
import org.jamesii.core.simulationrun.stoppolicy.SimTimeStopFactory
import org.jamesii.core.simulationrun.stoppolicy.WallClockTimeStopFactory
import org.jamesii.SimSystem
import sessl.VariableAssignment
import sessl.stringToParamName
import sessl.util.AlgorithmSet
import sessl.VarRange
import sessl.AbstractExperiment
import sessl.AbstractObservation
import sessl.AfterSimSteps
import sessl.AfterSimTime
import sessl.AfterWallClockTime
import sessl.AllSimulators
import sessl.AnySimulator
import sessl.ConjunctiveReplicationCondition
import sessl.ConjunctiveStoppingCondition
import sessl.DisjunctiveReplicationCondition
import sessl.DisjunctiveStoppingCondition
import sessl.FixedNumber
import sessl.MeanConfidenceReached
import sessl.MultipleVars
import sessl.Never
import sessl.Param
import sessl.ReplicationCondition
import sessl.Simulator
import sessl.StoppingCondition
import sessl.VarSeq
import sessl.Variable
import org.jamesii.simspex.adaptiverunner.policies.EpsilonGreedyDecrInitFactory
import org.jamesii.simspex.adaptiverunner.AdaptiveTaskRunnerFactory
import org.jamesii.core.model.IModel
import org.jamesii.core.experiments.replication.plugintype.RepCriterionFactory
import org.jamesii.core.experiments.replication.RepNumberCriterionFactory

/**
 * Encapsulates the BaseExperiment.
 *
 *  @see james.core.experiments.BaseExperiment
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

  /** Flag to check whether the experiment has been stopped properly already. */
  private[this] var experimentStopped = false

  /**
   * Stores all additional replication criteria to e configured configured.
   * This can be used, for example, by methods that need a certain minimal number of replications but
   * this does not need to be configured by the experimenter.
   * Their semantics is conjunctive, i.e. all additional conditions need to be fulfilled for termination.
   */
  val additionalReplicationConditions = ListBuffer[ReplicationCondition]()

  /** Encapsulated base experiment. */
  val exp = new BaseExperiment
  exp.setBackupEnabled(false)

  /** Allow to specify a model class. */
  def model_=(modelClass: Class[_ <: IModel]) = {
    modelLocation = Some(Experiment.URI_PREFIX_IMPL_MODELS + modelClass.getCanonicalName)
  }

  /** Basic configuration: basic stopping / replication options, rng setup, experiment variables. */
  override def basicConfiguration() = {
    configureModelLocation()
    configureStopping()
    configureSimulator()
    configureReplications()
    configureRNG()
    createExperimentVariables()
    defineFixedModelVariables()
  }

  /** Configure replications. */
  def configureReplications() =
    exp.setReplicationCriterionFactory(
      createRepCriterionParamFactory(
        createReplicationCriterion(
          additionalReplicationConditions.toList.foldLeft[ReplicationCondition](
            checkAndGetReplicationCondition())((l, r) => ConjunctiveReplicationCondition(l, r)))))

  def createRepCriterionParamFactory(r: IReplicationCriterion) =
    new ParameterizedFactory[RepCriterionFactory](new RepCriterionFactory() {
      override def create(params: ParamBlock): IReplicationCriterion = r
    })

  /** Create replication criterion. */
  def createReplicationCriterion(r: ReplicationCondition): IReplicationCriterion = r match {
    case r: FixedNumber => new ReplicationNumberCriterion(r.replications)
    case c: ConjunctiveReplicationCondition => new IReplicationCriterion() {
      val left = createReplicationCriterion(c.left)
      val right = createReplicationCriterion(c.right)
      override def sufficientReplications(ris: java.util.List[RunInformation]) =
        scala.math.max(left.sufficientReplications(ris), right.sufficientReplications(ris))
    }
    case d: DisjunctiveReplicationCondition => new IReplicationCriterion() {
      val left = createReplicationCriterion(d.left)
      val right = createReplicationCriterion(d.right)
      override def sufficientReplications(ris: java.util.List[RunInformation]) =
        scala.math.min(left.sufficientReplications(ris), right.sufficientReplications(ris))
    }
    case ci: MeanConfidenceReached => {
      require(this.isInstanceOf[AbstractObservation], "The replication criterion '" + ci +
        "' is specified, which works on observed data, but no observation is defined. Use '... with Observation'.")
      val self = this.asInstanceOf[AbstractObservation]
      require(self.reverseVariableBindings.contains(ci.varName), "The variable '" + ci.varName +
        "' has not been bound to a model variable yet.")
      val rv = new ConfidenceIntervalCriterion(ci.relativeHalfWidth, ci.confidence, Int.MaxValue, 1, 10, 12345L, //TODO: last two parameters, where are they from?
        self.reverseVariableBindings(ci.varName))
      throw new IllegalArgumentException("Mean confidence replication criterion not implemented yet.")
    }
    case x => throw new IllegalArgumentException("Replication criterion '" + x + "' not implemented yet.")
  }

  /** Configure model location. */
  def configureModelLocation() = {
    exp.setModelLocation(new URI(modelLocation.get))
  }

  /** Configure stopping. */
  def configureStopping() = {
    exp.setComputationTaskStopPolicyFactory(createParamStopFactory(checkAndGetStoppingCondition()))
  }

  /** Create parameterized stop factory. */
  def createParamStopFactory(s: StoppingCondition): ParameterizedFactory[StopFactory] = s match {
    case Never => new ParamFactory[StopFactory](new EmptyStopConditionStopPolicyFactory(), Param())
    case st: AfterSimTime => { checkSimTimeValidity(st); new ParamFactory[StopFactory](new SimTimeStopFactory(), Param() :/ (SimTimeStopFactory.SIMEND ~>> st.time)) }
    case ssteps: AfterSimSteps => new ParamFactory[StopFactory](new StepCountStopFactory(), Param() :/ (StepCountStopFactory.TASKEND ~>> ssteps.steps))
    case w: AfterWallClockTime => new ParamFactory[StopFactory](new WallClockTimeStopFactory(), Param() :/ (WallClockTimeStopFactory.SIMEND ~>> w.toMilliSeconds))
    case c: ConjunctiveStoppingCondition =>
      new ParamFactory[StopFactory](new ConjunctiveSimRunStopPolicyFactory(), Param() :/
        (CompositeCompTaskStopPolicyFactory.POLICY_FACTORY_LIST ~>> listParamStopFactories(createParamStopFactory(c.left), createParamStopFactory(c.right))))
    case d: DisjunctiveStoppingCondition =>
      new ParamFactory[StopFactory](new DisjunctiveSimRunStopPolicyFactory(), Param() :/
        (CompositeCompTaskStopPolicyFactory.POLICY_FACTORY_LIST ~>> listParamStopFactories(createParamStopFactory(d.left), createParamStopFactory(d.right))))
    case x => throw new IllegalArgumentException("Stopping criterion '" + s + "' not supported.")
  }

  def checkSimTimeValidity(st: AfterSimTime) = {
    require(st.toMilliSeconds == 0,
      "Duration of simulation time needs to be set via the 'time' property, as it is unit-less in JAMES II (do not use seconds, minutes, etc.).")
    if (st.time <= 0)
      logger.warn("Simulation time must is not positive (> 0), but set to:" + st.time)
  }

  /** Creates a list of Java objects containing the given parameterized factories. */
  private def listParamStopFactories(factories: ParameterizedFactory[ComputationTaskStopPolicyFactory]*) = {
    val rv = new java.util.ArrayList[JamesPair[ComputationTaskStopPolicyFactory, ParamBlock]]()
    factories.foreach(f => rv.add(new JamesPair[ComputationTaskStopPolicyFactory, ParamBlock](f.getFactory(), f.getParameters())))
    rv
  }

  /** Configure simulator. */
  def configureSimulator() = {
    if (simulators.size == 1)
      useFirstSetupAsProcessor()
    else if (simulators.size > 1)
      configureMultiSimulatorExperiment()
  }

  /** Configures experiment for multiple simulation algorithms. */
  final def configureMultiSimulatorExperiment() = {
    SimSystem.report(Level.INFO, "Configuring multi-simulator experiment with mode: " + executionMode)
    executionMode match {
      case AllSimulators => {
        val repsPerSetup = fixedReplications.getOrElse(1)
        logger.info("Configuring experiment for " + (repsPerSetup * simulators.size) + " replications")
        additionalReplicationConditions += FixedNumber(repsPerSetup * simulators.size)
        configureAdaptiveRunner(1, simulators, repsPerSetup)
      }
      case AnySimulator => configureAdaptiveRunner(1, simulators)
      case x => throw new IllegalArgumentException("Execution mode '" + x + "' is not supported.")
    }
  }

  /** Specifies the first given simulator setup as the processor to be used. */
  def useFirstSetupAsProcessor() =
    setProcessorParameters(ParamBlockGenerator.createParamBlock(simulators.algorithms(0).asInstanceOf[JamesIIAlgo[Factory]]))

  /**
   * Configure experiment to use the adaptive task runner.
   *
   *  @param threads
   *          the number of threads to be used
   *  @param setups
   *          the setups to be used
   *  @param requiredInitialRounds
   *          the required number of initial rounds
   */
  protected[james] def configureAdaptiveRunner(threads: Int, setups: AlgorithmSet[Simulator], requiredInitialRounds: Int = 1) = {
    //Convert setups to list of parameter blocks
    val parameterBlocks = ParamBlockGenerator.createParamBlockSet(setups.asInstanceOf[AlgorithmSet[JamesIIAlgo[Factory]]]).toList
    val paramBlockList = new java.util.ArrayList[ParamBlock]()
    parameterBlocks.foreach(p => paramBlockList.add(p))

    //Define parameters of the task runner
    var runnerParameters = Param() :/
      (ParallelComputationTaskRunnerFactory.NUM_CORES ~>> threads,
        AdaptiveTaskRunnerFactory.PORTFOLIO ~>> paramBlockList)
    if (requiredInitialRounds > 1)
      runnerParameters = runnerParameters :/ Param(AdaptiveTaskRunnerFactory.POLICY, classOf[EpsilonGreedyDecrInitFactory].getName,
        Param(EpsilonGreedyDecrInitFactory.MIN_NUM_TRIALS, requiredInitialRounds))

    //Set task runner
    exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new AdaptiveTaskRunnerFactory, runnerParameters))
  }

  /** Sets parameter block for processor factory. */
  protected[james] def setProcessorParameters(params: ParamBlock) =
    exp.getParameters().getParameterBlock().addSubBlock(classOf[ProcessorFactory].getName, params)

  /** Configure random number generation. */
  def configureRNG() = if (randomNumberGenerator.isDefined) {
    val customRNG = randomNumberGenerator.get.asInstanceOf[SimpleJamesIIRNG]
    SimSystem.getRNGGenerator().setRNGFactory(new ParameterizedFactory[RandomGeneratorFactory](customRNG.factory));
    SimSystem.getRNGGenerator().setSeed(customRNG.seed)
  }

  override def executeExperiment() = {
    addExecutionListener(exp)
    exp.execute()
    //This additional synchronization is necessary because JAMES II does currently not guarantee to call the 
    //ExperimentExecutionListener#experimentExecutionStopped(...) before BaseExperiment#execute(...) returns:
    exp.synchronized {
      if (!experimentStopped) {
        exp.wait
      }
    }
  }

  /**
   * Adds the execution listener.
   *  @param exp the James II experiment
   */
  private def addExecutionListener(experiment: BaseExperiment) = {
    experiment.getExecutionController().addExecutionListener(new ExperimentExecutionAdapter {

      //TODO: Remove as soon as this issue gets resolved in JAMES II
      private[this] var expStoppedCalled = false
      private[this] var runningSimulations = 0

      override def simulationInitialized(taskRunner: ITaskRunner,
        crti: ComputationTaskRuntimeInformation) = {
        val configSetup = Experiment.taskConfigToAssignment(crti.getComputationTask.getConfig)
        addAssignmentForRun(crti.getComputationTaskID.toString.hashCode, configSetup._1, configSetup._2)
        this.synchronized { //TODO: Remove as soon as this issue gets resolved in JAMES II
          runningSimulations = runningSimulations + 1
        }
      }
      override def simulationExecuted(taskRunner: ITaskRunner,
        crti: ComputationTaskRuntimeInformation, jobDone: Boolean) = {
        runDone(crti.getComputationTaskID.toString.hashCode)
        if (jobDone)
          replicationsDone(crti.getComputationTask.getConfig.getNumber)
        this.synchronized { //TODO: Remove as soon as this issue gets resolved in JAMES II
          runningSimulations = runningSimulations - 1
          finishExperimentIfNecessary()
        }
      }
      override def experimentExecutionStopped(be: BaseExperiment): Unit = {
        this.synchronized { //TODO: Remove as soon as this issue gets resolved in JAMES II
          expStoppedCalled = true
          finishExperimentIfNecessary()
        }
      }

      //TODO: Remove the following functions as soon as this issue gets resolved in JAMES II
      private[this] def experimentFinished = expStoppedCalled && runningSimulations == 0

      private[this] def finishExperimentIfNecessary() = if (experimentFinished) notifyExperimentOnFinish()

      private[this] def notifyExperimentOnFinish() = exp.synchronized {
        experimentDone()
        experimentStopped = true
        exp.notifyAll
      }
    })
  }

  /** Create experiment variables according to specified structure. */
  def createExperimentVariables() = {
    val variableStructure: java.util.List[java.util.List[ExperimentVariable[_]]] = new ArrayList
    for (variable <- variablesToScan)
      variable match {
        case multipleVars: MultipleVars => {
          val varsInLevel: java.util.List[ExperimentVariable[_]] = new ArrayList
          for (v <- multipleVars.variables)
            varsInLevel.add(createExperimentVariable(v))
          variableStructure.add(varsInLevel)
        }
        case variable: Variable => {
          val varsInLevel: java.util.List[ExperimentVariable[_]] = new ArrayList
          varsInLevel.add(createExperimentVariable(variable))
          variableStructure.add(varsInLevel)
        }
      }
    if (!variableStructure.isEmpty())
      exp.setupVariablesStructure(variableStructure)
  }

  /** Set variables that are fixed for each run. */
  def defineFixedModelVariables() = {
    for (v <- fixedVariables)
      exp.getFixedModelParameters().put(v._1, v._2)
  }

  /**
   * Creates the experiment variable.
   *
   *  @param variable the variable to be scanned
   *  @return the experiment variable
   */
  def createExperimentVariable(variable: Variable): ExperimentVariable[_] = variable match {
    case s: VarSeq => createSequenceVariable(s)
    case v: VarRange[_] => createRangeVariable(v)
    case x => throw new IllegalArgumentException("'" + x + "' is unknown, cannot be converted to experiment variable.")
  }

  /**
   * Creates an experiment variable for a sequence.
   *
   *  @param sequence the given sequence
   *  @return the experiment variable
   */
  def createSequenceVariable(sequence: VarSeq) = {
    val elems = new ArrayList[Any]
    require(typesAreEqual(sequence.values), "The types of objects in a sequence should be equal.")
    for (v <- sequence.values)
      elems.add(v)
    new ExperimentVariable[Any](sequence.name, createSequenceModifier(elems))
  }

  /**
   * Creates a sequence modifier for a given list of elements.
   *
   *  @param <T>
   *            the type of the list elements (and the returned modifier)
   *  @param elements
   *            the elements
   *  @return the sequence modifier
   */
  def createSequenceModifier[T](elements: java.util.List[T]):IVariableModifier[T] = new SequenceModifier[T](elements)

  /**
   * Checks whether all values in the gives sequence are of the same type.
   *
   *  @param values
   *            the values
   *  @return true, if all values have the same type
   */
  def typesAreEqual(values: Seq[Any]): Boolean = sessl.util.MiscUtils.typesAreEqual(values) {
    x: Any => report(Level.SEVERE, "Type of '" + x + "' (" + x.getClass() + ") does not match type of first element: " + values.head.getClass())
  }

  /**
   * Creates an experiment variable for a range variable.
   *
   *  @param <T>
   *            the type of the variable's values
   *  @param varRange
   *            the variable with a range
   *  @return the james experiment variable
   */
  def createRangeVariable[T <: AnyVal](varRange: VarRange[T]) = {
    new ExperimentVariable[T](varRange.name, createIncrementModifier(varRange))
  }

  /**
   * Creates the increment modifier for a given range.
   *
   *  @param <T>
   *            the type of the variable's values (has to be Int, Double, or Long)
   *  @param varRange
   *            the variable with a range
   *  @return the variable  modifier
   */
  def createIncrementModifier[T <: AnyVal](varRange: VarRange[T]): IVariableModifier[T] = {
    require(typesAreEqual(Seq(varRange.from, varRange.step, varRange.to)), "The types of the from/step/to values of a range should be equal.")
    if (varRange.from.isInstanceOf[java.lang.Integer])
      return (new IncrementModifierInteger(varRange.from.asInstanceOf[Int], varRange.step.asInstanceOf[Int], varRange.to.asInstanceOf[Int])).asInstanceOf[IVariableModifier[T]]
    if (varRange.from.isInstanceOf[java.lang.Long])
      return (new IncrementModifierInteger(varRange.from.asInstanceOf[Long].toInt, varRange.step.asInstanceOf[Long].toInt, varRange.to.asInstanceOf[Long].toInt)).asInstanceOf[IVariableModifier[T]]
    if (varRange.from.isInstanceOf[java.lang.Double])
      return (new IncrementModifierDouble(varRange.from.asInstanceOf[Double], varRange.step.asInstanceOf[Double], varRange.to.asInstanceOf[Double])).asInstanceOf[IVariableModifier[T]]
    throw new IllegalArgumentException("Type '" + varRange.from.getClass() + "' is not supported by range (Integer, Long, and Double are supported).")
  }

}

object Experiment {

  /** The prefix for URIs that point to implemented models. */
  private val URI_PREFIX_IMPL_MODELS = "java://"

  /** Extract variable assignment and configuration ID from computation task configuration. */
  def taskConfigToAssignment(taskConfig: IComputationTaskConfiguration): (Int, VariableAssignment) = {
    val config = taskConfig match {
      case tc: SimulationRunConfiguration => tc
      case _ => throw new IllegalArgumentException("Only simulation run configurations supported so far...")
    }
    val assignment = ListBuffer[(String, Any)]()
    val it = config.getParameters.entrySet.iterator
    while (it.hasNext) {
      val entry = it.next
      entry.getValue match {
        // Remove base variables:
        case bv: BaseVariable[_] => { assignment += ((entry.getKey, bv.getValue())) }
        case _ => { assignment += ((entry.getKey, entry.getValue)) }
      }
    }
    (config.getNumber(), assignment.toList)
  }

}
