package sessl.james

import james.core.experiments.optimization.parameter.cancellation.ICancelOptimizationCriterion
import james.core.experiments.optimization.parameter.cancellation.TotalConfigurationCancellation
import james.core.experiments.optimization.parameter.cancellation.TotalWallclockCancellation
import james.core.experiments.optimization.parameter.instrumenter.IResponseObsModelInstrumenter
import james.core.experiments.optimization.parameter.instrumenter.IResponseObsSimInstrumenter
import james.core.experiments.optimization.parameter.representativeValue.ArithmeticMeanOfObjectives
import james.core.experiments.optimization.parameter.Configuration
import james.core.experiments.optimization.parameter.IOptimizationObjective
import james.core.experiments.optimization.parameter.OptimizationProblemDefinition
import james.core.experiments.optimization.OptimizationVariables
import james.core.experiments.optimization.Optimizer
import james.core.experiments.optimization.OptimizerVariable
import james.core.experiments.variables.ExperimentVariables
import james.core.model.variables.BaseVariable
import james.core.model.variables.ByteVariable
import james.core.model.variables.DoubleVariable
import james.core.model.variables.IntVariable
import james.core.model.variables.LongVariable
import james.core.model.variables.ShortVariable
import steering.configuration.optimization.evolutionary.BasicHillClimbingOptimizerFactory
import james.core.experiments.optimization.OptimizationStatistics

import sessl._

/**
 * Support for configuring James II for optimization.
 *
 * @author Roland Ewald
 *
 */
trait Optimization extends AbstractOptimization {
  this: ExperimentOn with Instrumentation =>

  /** The type used in James II  to specify start configurations. */
  type JamesParamConfig = _root_.james.core.experiments.optimization.parameter.Configuration

  private type CancelCriterion = ICancelOptimizationCriterion

  private type OptStats = OptimizationStatistics

  abstract override def configure() {
    super.configure()
    configureOptimizationProblem()
    val optimizationVariables = createOptimizationVariables()
    registerExperimentVariables(optimizationVariables)
  }

  /** The problem definition can stay empty, as now additional instrumenters need to be defined here. */
  private[this] val optProb = new OptimizationProblemDefinition() {
    def getModelInstrumenter(): IResponseObsModelInstrumenter = null
    def getSimulationInstrumenter(): IResponseObsSimInstrumenter = null
  }

  /** Configures the user-specific details of the optimization problem. */
  private[this] def configureOptimizationProblem() = {
    optProb.addOptimizationObjective(new IOptimizationObjective() {
      override def calcObjective(config: Configuration, results: java.util.Map[String, BaseVariable[_]]): Double = {
        require(results.containsKey(Instrumentation.INSTRUMENTATION_RESULTS), "Results should have been packaged in base variable called '" + Instrumentation.INSTRUMENTATION_RESULTS + "'")
        val baseVar = results.get(Instrumentation.INSTRUMENTATION_RESULTS)
        objectiveFunction.get.apply(baseVar.getValue().asInstanceOf[InstrumentationRunResultsAspect]).doubleValue()
      }
      override def getName() = "sessl objective"
    })

    for (startConfig <- startConfigurations)
      optProb.addPredefinedConfiguration(createPredefConfig(startConfig))
    optProb.setRepresentedValueCalculation(new ArithmeticMeanOfObjectives)
    optProb.addCancelOptimizationCriteria(createCancelCriteria(optStopPolicy)) //TODO: configure this properly
  }

  /** Creates (nested) optimization cancel criterion. */
  private def createCancelCriteria(policy: OptimizationStopPolicy): CancelCriterion =
    policy match {
      case OptMaxAssignments(num) => new TotalConfigurationCancellation(num)
      case o: OptMaxTime => new TotalWallclockCancellation(o.toMilliSeconds)
      case ConjunctiveOptimizationStopPolicy(left, right) =>
        joinCancelCriteria(createCancelCriteria(left), createCancelCriteria(right), _ && _)
      case DisjunctiveOptimizationStopPolicy(left, right) =>
        joinCancelCriteria(createCancelCriteria(left), createCancelCriteria(right), _ || _)
      case _ => throw new IllegalArgumentException("Stop policy '" + optStopPolicy + " of type " + optStopPolicy.getClass() + " is not supported.")
    }

  /** Creates joint cancel criterion; the decision function f checks whether the cancel criteria c1/2 are met. */
  private def joinCancelCriteria(c1: CancelCriterion, c2: CancelCriterion, f: (Boolean, Boolean) => Boolean): CancelCriterion = new CancelCriterion() {
    val firstCriteria = c1
    val secondCriteria = c2
    val decisionFunction = f
    override def meetsCancelCriterion(state: OptStats): Boolean = {
      decisionFunction(firstCriteria.meetsCancelCriterion(state), secondCriteria.meetsCancelCriterion(state))
    }
  }

  /** Create a start configuration from the variables specified by the user.*/
  def createPredefConfig(startConfig: OptVarConfiguration): JamesParamConfig = {
    val startConfigJames = new JamesParamConfig()
    for (variable <- startConfig)
      startConfigJames.put(variable.name, createBaseVar(variable.name, variable.value))
    startConfigJames
  }

  /** Create a base variable with given name and value. */
  def createBaseVar[T](name: String, value: T): BaseVariable[T] =
    {
      val baseVar = value match {
        case v: Double => new DoubleVariable(name, false, 1., v)
        case v: Integer => new IntVariable(name, v)
        case v: Long => new LongVariable(name, v)
        case v: Short => { val x = new ShortVariable(); x.setName(name); x.setValue(v); x }
        case v: Byte => { val x = new ByteVariable(); x.setName(name); x.setValue(v); x }
        case _ => new BaseVariable[T](name)
      }
      baseVar.asInstanceOf[BaseVariable[T]]
    }

  /** Creates experiment variables structure. */
  private[this] def createOptimizationVariables(): ExperimentVariables = {
    val optimizationAlgorithms = new Array[Optimizer](1)
    //TODO: Change parameter setup of optimizers!
    optimizationAlgorithms(0) = new Optimizer(optimizationAlgorithm.get.asInstanceOf[BasicJamesIIOptimizer].factory.create(
      Param("", optProb) :/ BasicHillClimbingOptimizerFactory.START_CONFIGURATION ~> createPredefConfig(startConfigurations(0))), optProb)
    val optimizerVariable = new OptimizerVariable(optimizationAlgorithms)
    val experimentVariables = new ExperimentVariables
    experimentVariables.addVariable(optimizerVariable)
    val optimizationVariables = new OptimizationVariables
    experimentVariables.setSubLevel(optimizationVariables)
    experimentVariables
  }

  /** Adds the experiment variables for optimization to the overall experiment. */
  private[this] def registerExperimentVariables(optimizationVars: ExperimentVariables) = {
    if (!optimizeOnAllConfigs) { // Add at the bottom of the hierarchy...
      if (exp.getExperimentVariables == null)
        exp.setExperimentVariables(optimizationVars)
      else exp.getExperimentVariables.getLowestSubLevel.setSubLevel(optimizationVars)
    } else { // ... or at the top
      optimizationVars.getLowestSubLevel().setSubLevel(exp.getExperimentVariables)
      exp.setExperimentVariables(optimizationVars)
    }
  }
}