package sessl.james

import java.net.URI
import java.util.logging.Level
import java.util.ArrayList
import james.core.experiments.taskrunner.ITaskRunner
import james.core.experiments.tasks.stoppolicy.plugintype.ComputationTaskStopPolicyFactory
import james.core.experiments.tasks.stoppolicy.steps.StepCountStopFactory
import james.core.experiments.tasks.stoppolicy.EmptyStopConditionStopPolicyFactory
import james.core.simulationrun.stoppolicy.CompositeCompTaskStopPolicyFactory
import james.core.simulationrun.stoppolicy.ConjunctiveSimRunStopPolicyFactory
import james.core.simulationrun.stoppolicy.DisjunctiveSimRunStopPolicyFactory
import james.core.simulationrun.stoppolicy.WallClockTimeStopFactory
import james.core.simulationrun.stoppolicy.SimTimeStopFactory
import james.core.experiments.variables.modifier.IVariableModifier
import james.core.experiments.variables.modifier.IncrementModifierDouble
import james.core.experiments.variables.modifier.IncrementModifierInteger
import james.core.experiments.variables.modifier.SequenceModifier
import james.core.experiments.variables.ExperimentVariable
import james.core.experiments.steering.SteeredExperimentVariables
import james.core.experiments.BaseExperiment
import james.core.experiments.ComputationTaskRuntimeInformation
import james.core.model.IModel
import james.core.parameters.ParameterizedFactory
import james.core.processor.plugintype.ProcessorFactory
import james.core.experiments.replication.ReplicationNumberCriterion
import james.core.experiments.replication.ConfidenceIntervalCriterion
import james.core.experiments.replication.IReplicationCriterion
import james.core.experiments.RunInformation
import james.core.experiments.steering.IExperimentSteerer
import james.core.experiments.variables.ExperimentVariables
import james.core.experiments.steering.VariablesAssignment
import james.SimSystem
import james.core.experiments.steering.ExperimentSteererVariable
import simspex.exploration.simple.SimpleSimSpaceExplorer
import james.core.experiments.IComputationTaskConfiguration
import james.core.experiments.SimulationRunConfiguration
import james.core.model.variables.BaseVariable
import james.core.parameters.ParameterBlock
import scala.collection.mutable.ListBuffer
import sessl.util.AlgorithmSet
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory
import james.core.experiments.taskrunner.plugintype.TaskRunnerFactory
import simspex.adaptiverunner.AdaptiveTaskRunnerFactory
import simspex.adaptiverunner.policies.EpsilonGreedyDecrInitFactory

import sessl._
import sessl.james._

/** Encapsulates the BaseExperiment.
 *
 *  @see james.core.experiments.BaseExperiment
 *
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

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
  def configureReplications() = exp.addReplicationCriterion(createReplicationCriterion(checkAndGetReplicationCondition()))

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
      require(this.isInstanceOf[AbstractInstrumentation], "The replication criterion '" + ci +
        "' is specified, which works on observed data, but no instrumentation is defined. Use '... with Instrumentation'.")
      val self = this.asInstanceOf[AbstractInstrumentation]
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
    exp.setComputationTaskStopFactory(createParamStopFactory(checkAndGetStoppingCondition()))
  }

  /** Create parameterized stop factory. */
  def createParamStopFactory(s: StoppingCondition): ParameterizedFactory[StopFactory] = s match {
    case Never => new ParamFactory[StopFactory](new EmptyStopConditionStopPolicyFactory(), Param())
    case st: AfterSimTime => new ParamFactory[StopFactory](new SimTimeStopFactory(), Param() :/ (SimTimeStopFactory.SIMEND ~> st.time))
    case ssteps: AfterSimSteps => new ParamFactory[StopFactory](new StepCountStopFactory(), Param() :/ (StepCountStopFactory.TASKEND ~> ssteps.steps))
    case w: AfterWallClockTime => new ParamFactory[StopFactory](new WallClockTimeStopFactory(), Param() :/ (WallClockTimeStopFactory.SIMEND ~> w.toMilliSeconds))
    case c: ConjunctiveStoppingCondition =>
      new ParamFactory[StopFactory](new ConjunctiveSimRunStopPolicyFactory(), Param() :/
        (CompositeCompTaskStopPolicyFactory.POLICY_FACTORY_LIST ~> listParamStopFactories(createParamStopFactory(c.left), createParamStopFactory(c.right))))
    case d: DisjunctiveStoppingCondition =>
      new ParamFactory[StopFactory](new DisjunctiveSimRunStopPolicyFactory(), Param() :/
        (CompositeCompTaskStopPolicyFactory.POLICY_FACTORY_LIST ~> listParamStopFactories(createParamStopFactory(d.left), createParamStopFactory(d.right))))
    case x => throw new IllegalArgumentException("Stopping criterion '" + s + "' not supported.")
  }

  /** Creates a list of Java objects containing the given parameterized factories. */
  private def listParamStopFactories(factories: ParameterizedFactory[ComputationTaskStopPolicyFactory]*) = {
    val rv = new java.util.ArrayList[JamesPair[ComputationTaskStopPolicyFactory, ParamBlock]]()
    factories.foreach(f => rv.add(new JamesPair[ComputationTaskStopPolicyFactory, ParamBlock](f.getFactory(), f.getParameters())))
    rv
  }

  /** Configure simulator. */
  def configureSimulator() = {
    if (simulatorSet.size == 1)
      useFirstSetupAsProcessor()
    else if (simulatorSet.size > 1)
      configureMultiSimulatorExperiment()
  }

  /** Configures experiment for multiple simulation algorithms. */
  final def configureMultiSimulatorExperiment() = {
    SimSystem.report(Level.INFO, "Configuring multi-simulator experiment with mode: " + simulatorExecutionMode)
    simulatorExecutionMode match {
      case AllSimulators => {
        val repsPerSetup = fixedReplications.getOrElse(1)
        exp.addReplicationCriterion(new ReplicationNumberCriterion(repsPerSetup * simulatorSet.size))
        configureAdaptiveRunner(1, simulatorSet, repsPerSetup)
      }
      case AnySimulator => configureAdaptiveRunner(1, simulatorSet)
      case x => throw new IllegalArgumentException("Execution mode '" + x + "' is not supported.")
    }
  }

  /** Specifies the first given simulator setup as the processor to be used. */
  def useFirstSetupAsProcessor() =
    setProcessorParameters(ParamBlockGenerator.createParamBlock(simulatorSet.algorithms(0).asInstanceOf[JamesIIAlgo[Factory]]))

  /** Configure experiment to use the adaptive task runner.
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
      (ParallelComputationTaskRunnerFactory.NUM_CORES ~> threads,
        AdaptiveTaskRunnerFactory.PORTFOLIO ~> paramBlockList)
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
    SimSystem.getRNGGenerator().setRngFactoryName(customRNG.factory.getClass.getCanonicalName)
    SimSystem.getRNGGenerator().setInitialSeed(customRNG.seed)
  }

  override def execute() = {
    addExecutionListener(exp)
    exp.execute()
    experimentDone()
  }

  /** Adds the execution listener.
   *  @param exp the James II experiment
   */
  private def addExecutionListener(exp: BaseExperiment) = { //TODO: THIS IS IMPORTANT FOR INTEGRATING OTHER SYSTEMS --- DOCUMENT THIS!
    exp.getExecutionController().addExecutionListener(new ExperimentExecutionAdapter {
      override def simulationInitialized(taskRunner: ITaskRunner,
        crti: ComputationTaskRuntimeInformation) = {
        val configSetup = Experiment.taskConfigToAssignment(crti.getComputationTask.getConfig)
        addAssignmentForRun(crti.getComputationTaskID.toString.hashCode, configSetup._1, configSetup._2)
      }
      override def simulationExecuted(taskRunner: ITaskRunner,
        crti: ComputationTaskRuntimeInformation, jobDone: Boolean) = {
        runDone(crti.getComputationTaskID.toString.hashCode)
        if (jobDone)
          replicationsDone(crti.getComputationTask.getConfig.getNumber) //TODO: This is important - EXTRACT AS METHODS!!! 
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

  /** Creates the experiment variable.
   *
   *  @param variable the variable to be scanned
   *  @return the experiment variable
   */
  def createExperimentVariable(variable: Variable): ExperimentVariable[_] = variable match {
    case s: VarSeq => createSequenceVariable(s)
    case v: VarRange[_] => createRangeVariable(v)
    case x => throw new IllegalArgumentException("'" + x + "' is unknown, cannot be converted to experiment variable.")
  }

  /** Creates an experiment variable for a sequence.
   *
   *  @param sequence the given sequence
   *  @return the experiment variable
   */
  def createSequenceVariable(sequence: VarSeq) = {
    val elems = new ArrayList[Any]
    require(typesAreEqual(sequence.values), "The types of objects in a sequence should be equal.")
    for (v <- sequence.values)
      elems.add(v)
    new ExperimentVariable(sequence.name, sequence.values.head, createSequenceModifier(elems))
  }

  /** Creates a sequence modifier for a given list of elements.
   *
   *  @param <T>
   *            the type of the list elements (and the returned modifier)
   *  @param elements
   *            the elements
   *  @return the sequence modifier
   */
  def createSequenceModifier[T](elements: java.util.List[T]) = new SequenceModifier[T](elements)

  /** Checks whether all values in the gives sequence are of the same type.
   *
   *  @param values
   *            the values
   *  @return true, if all values have the same type
   */
  def typesAreEqual(values: Seq[Any]): Boolean = sessl.util.MiscUtils.typesAreEqual(values) {
    x: Any => report(Level.SEVERE, "Type of '" + x + "' (" + x.getClass() + ") does not match type of first element: " + values.head.getClass())
  }

  /** Creates an experiment variable for a range variable.
   *
   *  @param <T>
   *            the type of the variable's values
   *  @param varRange
   *            the variable with a range
   *  @return the james experiment variable
   */
  def createRangeVariable[T <: AnyVal](varRange: VarRange[T]) = {
    new ExperimentVariable[T](varRange.name, varRange.from, createIncrementModifier(varRange))
  }

  /** Creates the increment modifier for a given range.
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