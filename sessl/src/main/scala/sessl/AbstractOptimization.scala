package sessl

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

/** Support for optimization.
 *
 *  @author Roland Ewald
 *
 */
trait AbstractOptimization extends ExperimentConfiguration {
  this: AbstractExperiment with AbstractObservation =>

  /** The type of the objective function to be used. */
  type ObjectiveFunction = ObservationRunResultsAspect => Number

  /** The type that specifies how a start configuration looks like. */
  type OptVarConfiguration = List[VarSingleVal]

  /** If not specified otherwise, a default observation time at 0.95 * stop time will be configured. */
  val defaultObsStopTimeFactor = 0.95

  /** Defines whether the optimizer operates on the whole parameter space as defined by the variables to be scanned (so this flag is true),
   *  or if its optimized each single parameter combination (so this flag is false, which is the default).
   */
  protected var optimizeOnAllConfigs = false

  /** The variables to be optimized. */
  protected val optVariables = ListBuffer[VarRange[_]]()

  /** The configurations from which the optimizer may start.
   *  If none is set, one should be generated randomly.
   */
  private[this] val startConfigs = ListBuffer[OptVarConfiguration]()

  /** The name bindings for the variable to be optimized: internal name => sessl name. */
  protected val optVariableBindings = Map[String, String]()

  /** The reverse bindings map: sessl name => internal name. */
  protected val optVariableBindingsReversed = Map[String, String]()

  /** The optimization algorithm to be used. */
  protected[sessl] var optimizationAlgorithm: Option[Optimizer] = None

  /** The objective function to be used. */
  protected[sessl] var objectiveFunction: Option[ObjectiveFunction] = None

  /** The optimization stop condition. Per default, only the first variable assignment is simulated.*/
  var optStopCondition: OptimizationStopCondition = OptMaxAssignments(1)

  /** Set/get optimization algorithm. */
  def optimizer_=(opt: Optimizer) = { optimizationAlgorithm = Some(opt) }
  def optimizer: Optimizer = { optimizationAlgorithm.get }

  /** Configure optimization. */
  def optimizeFor(varsInOptFunction: DataElemBinding*)(optFunction: ObjectiveFunction) {
    varsInOptFunction.map(observe(_))
    objectiveFunction = Some(optFunction)
  }

  /** Get the default observation time to be used. */
  def defaultObsTime = defaultObsStopTimeFactor * stopTime

  override def configure() {
    require(optimizationAlgorithm.isDefined, "No optimization algorithm is defined, use 'optimizer = ' to set one.")
    if (!isObservationTimingDefined && fixedStopTime.isDefined) {
      val defaultObsTime = defaultObsStopTimeFactor * stopTime
      observeAt(defaultObsTime)
      println("Warning: no observation times are set, using " + scala.math.round(defaultObsStopTimeFactor * 100) + "% of stop time: " + defaultObsTime) //TODO: Use logging here
    }
    require(isObservationTimingDefined, "No times for observation are set (use observeAtTimes(...)), and no fixed stop time is given, ie no default value can be set.")
    super.configure()
  }

  /** Register variables to be optimized.
   *  @param binding a binding specification between an internal name and a sessl name
   *  @param variableRange the range in which the variable shall be optimized
   */
  def optimize[T <: AnyVal](binding: DataElemBinding, variableRange: ValueRange[T]) = {
    optVariableBindings += ((binding.internalName, binding.sesslName))
    optVariableBindingsReversed += ((binding.sesslName, binding.internalName))
    optVariables += VarRange(binding.internalName, variableRange.from, variableRange.step, variableRange.to)
  }

  /** Allows to define a start configuration. */
  def startOptimizationWith(variablesToSet: Variable*) {

    val variableList = variablesToSet.toList.map(
      v => v match {
        case v: VarSingleVal => {
          require(optVariableBindingsReversed.contains(v.name), "Variable with name '" + v.name + "' has not been bound to internal variable yet.")
          VarSingleVal(optVariableBindingsReversed.get(v.name).get, v.value)
        }
        case _ => throw new IllegalArgumentException("Variable '" + v.name + "' does not specify a single value!")
      })

    val startConfigVarNames = variableList.map(_.name).toSet
    val optVarNames = optVariables.map(_.name).toSet
    def sesslNameSet(names: Set[String]) = { names.map(optVariableBindings.get(_).getOrElse("Unbound name:" + _)).mkString(", ") }

    require(startConfigVarNames == optVarNames, "Sets of optimization variables and variables for the start configuration do not match! Start configuration defines '" +
      sesslNameSet(startConfigVarNames) + "', but optimizer variables are: '" + sesslNameSet(optVarNames) + "'.")

    startConfigs += variableList
  }

  /** Get list of start configurations. */
  def startConfigurations = startConfigs.toList

}

/** Super trait of all optimization stop conditions. */
trait OptimizationStopCondition

/** Stop after a certain number of assignments has been computed. */
case class OptMaxAssignments(number: Int) extends OptimizationStopCondition

/** Stop after a certain amount of time has been consumed. */
case class OptMaxTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) extends OptimizationStopCondition with AbstractDuration

/** Use two policies (combined with AND). */
case class ConjunctiveOptimizationStopCondition(left: OptimizationStopCondition, right: OptimizationStopCondition) extends OptimizationStopCondition

/** Use two policies (combined with OR). */
case class DisjunctiveOptimizationStopCondition(left: OptimizationStopCondition, right: OptimizationStopCondition) extends OptimizationStopCondition

/** Intermediate policy to provide and/or methods. */
case class CombinedOptimizationStopCondition(left: OptimizationStopCondition) extends OptimizationStopCondition {
  def or(right: OptimizationStopCondition) = new DisjunctiveOptimizationStopCondition(left, right)
  def and(right: OptimizationStopCondition) = new ConjunctiveOptimizationStopCondition(left, right)
}