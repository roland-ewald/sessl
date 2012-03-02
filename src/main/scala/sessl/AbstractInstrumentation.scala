package sessl

import scala.collection.mutable.Set
import scala.collection.mutable.Map

import sessl.util.ResultOperations

/** Support for instrumentation. The instrumentation trait is also concerned with managing the observed data in simple way,
 *  ie. it has to provide read-access to be used across other traits (mixed-in later).
 *
 *  @author Roland Ewald
 *
 */
abstract trait AbstractInstrumentation extends ExperimentConfiguration {
  this: AbstractExperiment =>

  /** The exact times at which the state shall be observed.*/
  private[this] var times: Option[List[Double]] = None

  /** The range of times at which the state shall be observed.*/
  private[this] var timeRange: Option[ValueRange[Double]] = None

  /** The mapping from internal names to sessl names.*/
  private val bindings: Map[String, Set[String]] = Map()

  /** Configuring observation at specific time steps.*/
  final def observeAtTimes(instrTimes: Double*)(instrFunc: => Unit) = {
    times = Some(instrTimes.toList)
    instrFunc
  }

  /** Configuring observation at a specific range of times. */
  final def observePeriodically(range: ValueRange[Double])(instrFunc: => Unit) = {
    timeRange = Some(range)
    instrFunc
  }

  /** Defines to 'bind' one variable name in sessl to a model/sim-specific ('internal') variable.*/
  def bind(binds: DataElemBinding*) = {
    for (binding <- binds) {
      val boundSesslNames = bindings.getOrElse(binding.internalName, Set()) += binding.sesslName
      bindings(binding.internalName) = boundSesslNames
    }
  }

  /** Add event handler that processes observed model output from a single run. */
  def withRunResult(f: InstrumentationRunResultsAspect => Unit) = {
    afterRun {
      r => f.apply(r.aspectFor(classOf[AbstractInstrumentation]).get.asInstanceOf[InstrumentationRunResultsAspect])
    }
  }

  /** Add event handler that processes observed model output from a set of replications. */
  def withReplicationsResult(f: InstrumentationReplicationsResultsAspect => Unit) = {
    afterReplications {
      r => f.apply(r.aspectFor(classOf[AbstractInstrumentation]).get.asInstanceOf[InstrumentationReplicationsResultsAspect])
    }
  }

  /** Add event handler that processes observed model output from the whole experiment. */
  def withExperimentResult(f: InstrumentationExperimentResultsAspect => Unit) = {
    afterExperiment {
      r => f.apply(r.aspectFor(classOf[AbstractInstrumentation]).get.asInstanceOf[InstrumentationExperimentResultsAspect])
    }
  }

  /** Get all time points at which the values of observed variables shall be stored. */
  lazy val observationTimes: List[Double] = {
    val unsortedTimes: List[Double] = {
      if (times.isDefined)
        times.get
      else if (timeRange.isDefined)
        timeRange.get.toList
      else {
        println("Warning: Neither specific times nor a time range is given for instrumentation.") //TODO: Use logging here
        Nil
      }
    }

    val sortedTimes = unsortedTimes.sorted
    if (!sortedTimes.isEmpty)
      require(!fixedStopTime.isDefined || (sortedTimes.last <= fixedStopTime.get), "Observation time of '" + sortedTimes.last +
        "' cannot be reached (fixed stop time is: '" + fixedStopTime.get + "'). Ordered list of configured observation times: " + sortedTimes.mkString(", "))
    sortedTimes
  }

  /** Allows to checks whether *some* observation times have been defined. */
  def isObservationTimingDefined = times.isDefined || timeRange.isDefined

  /** The bindings internalName => externalName. */
  def variableBindings = bindings.toMap

  /** The reverse bindings: externalName => internalName. Requires some computing effort, hence a lazy value. */
  lazy val reverseVariableBindings = {
    val rv = Map[String, String]()
    for (key <- variableBindings.keys; value <- variableBindings(key)) {
      require(!rv.contains(value), "The sessl variable '" + value + "' is bound to both '" + rv(value) + "' and to '" + key + "'. This is not allowed.")
      rv(value) = key
    }
    rv
  }

  /** The variables to be observed (internal model/sim-specific names). */
  def varsToBeObserved = bindings.keySet

  /** Before the run is done, add all results of this run to the experiment. */
  override def collectRunResultsAspects(runId: Int) {
    super.collectRunResultsAspects(runId)
    addRunResultsAspect(runId, collectResults(runId, true))
  }

  /** Before the replications are done, add all results of this run to the experiment. */
  override def collectReplicationsResultsAspects(assignId: Int) {
    super.collectReplicationsResultsAspects(assignId)
    addReplicationsResultsAspect(assignId, collectReplicationsResults(assignId))
  }

  /** Before the experiment is done, add result aspect for instrumentation. */
  override def collectExperimentResultsAspects() {
    super.collectExperimentResultsAspects()
    addExperimentResultsAspect(new InstrumentationExperimentResultsAspect())
  }

  /** Collects the results of the indicated run. This function may only be called ONCE per runID,
   *  afterwards the instrumentation sub-system may regard the data as read-out (and hence delete it).
   *
   *  @param runID the ID of the run
   *  @return the result aspect of the run (w.r.t. instrumentation)
   */
  def collectResults(runID: Int, removeData: Boolean): InstrumentationRunResultsAspect

  /** Signals that all results for the given configuration ID have been collected.
   *  Override to clean up auxiliary data structures.
   */
  def collectReplicationsResults(assignID: Int): InstrumentationReplicationsResultsAspect

}

/** Represents an association between a 'sessl'-variable and a model/sim-specific ('internal') variable. */
sealed case class DataElemBinding(sesslName: String, internalName: String)

/** Name of a data element (=> the first part of a data element binding). */
sealed case class DataElemName(sesslName: String) {
  def to(internalName: String) = new DataElemBinding(sesslName, internalName)
  def ~(internalName: String) = to(internalName)
}

/** The run results aspect for instrumentation.
 *  @param assignment the variable assignment that was used
 *  @param data the data recorded for a single run: variable name (in sessl) => trajectory.
 */
class InstrumentationRunResultsAspect(var data: Map[String, Trajectory]) extends RunResultsAspect(classOf[AbstractInstrumentation]) with ResultOperations {

  /** Auxiliary constructor to merge two result sets (e.g. recorded by different entities but for the same run).*/
  def this(aspects: (InstrumentationRunResultsAspect, InstrumentationRunResultsAspect)) = {
    this(aspects._1.data ++ aspects._2.data)
  }

  /** Get the *last* recorded value of the specified variable. */
  def apply(name: String) = {
    val values = getVarData(name)
    require(!values.isEmpty, "No values stored for variable '" + name + "'!")
    values.last._2
  }

  /** Checks whether a variable name is defined in the results. */
  def ?(varName: String) = data.contains(varName)

  /** Gets all data on a given variable name. */
  def trajectory(name: String): Trajectory = getVarData(name)

  /** Get all data on a given variable name, and includes the name.*/
  def ~(name: String): (String, Trajectory) = (name, getVarData(name))

  /** Get all times at which the data was observed. */
  def times(name: String): List[Double] = getVarData(name).map(_._1)

  /** Get all values that have been observed. */
  def values(name: String): List[_] = getVarData(name).map(_._2)

  /** Get the names of all defined variables. */
  lazy val names = data.keys.toList

  /** Checks whether variable is defined and returns it if possible. */
  private def getVarData(varName: String): Trajectory = {
    require(this ? varName, "Variable '" + varName + "' is not defined!")
    data.get(varName).get
  }

  override protected def getValuesFor(name: String) = values(name)
}

/** Replications results aspect for instrumentation. */
class InstrumentationReplicationsResultsAspect extends ReplicationsResultsAspect(classOf[AbstractInstrumentation]) with ResultOperations {

  /** Get the *last* recorded value of the specified variable for all runs for which it has been observed. */
  def apply(name: String) = {
    val values = runsResults.mapValues(_.asInstanceOf[InstrumentationRunResultsAspect](name)).values.toList
    values
  }

  /** Apply name to the result, combine results in *named* tuple.*/
  def ~(name: String) = (name, apply(name))

  override protected def getValuesFor(name: String) = apply(name)
}

/** Experiment results aspect for instrumentation. */
class InstrumentationExperimentResultsAspect extends ExperimentResultsAspect(classOf[AbstractInstrumentation]) with ResultOperations
  with PartialExperimentResults[InstrumentationExperimentResultsAspect] {

  /** Get the last sample for the given variable from all runs. */
  def apply(name: String) = runsResults.mapValues(_.asInstanceOf[InstrumentationRunResultsAspect](name)).values.toList

  /** Apply name to the result, combine results in *named* tuple.*/
  def ~(name: String) = (name, apply(name))

  override protected def getValuesFor(name: String) = apply(name)

  override def createPartialResult(runsResults: Map[Int, RunResultsAspect], replicationsResults: Map[Int, ReplicationsResultsAspect]): InstrumentationExperimentResultsAspect = {
    val aspect = new InstrumentationExperimentResultsAspect
    aspect.setResults(runsResults, replicationsResults)
    aspect
  }
}