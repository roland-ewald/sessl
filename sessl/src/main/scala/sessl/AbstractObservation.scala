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
package sessl

import scala.collection.mutable.Map
import scala.collection.mutable.Set

import sessl.util.MiscUtils
import sessl.util.ResultOperations

/**
 * Support for observation of model output. The observation trait is also concerned with managing the observed data in simple way,
 *  ie. provides read-access to be used across other traits (mixed-in later).
 *
 *  @author Roland Ewald
 *
 */
abstract trait AbstractObservation extends ExperimentConfiguration {
  this: AbstractExperiment =>

  /** The exact times at which the state shall be observed.*/
  private[this] var times: Option[List[Double]] = None

  /** The range of times at which the state shall be observed.*/
  private[this] var timeRange: Option[ValueRange[Double]] = None

  /** The mapping from internal names to sessl names.*/
  private val bindings: Map[String, Set[String]] = Map()

  /**
   * Observe at specific time steps.
   * @param observationTimes list of time points at which observation snapshots shall be taken
   * @example {{{
   * 	observerAt(0.2, 10.2, 35.4) // Triggers observation after 0.2, 10.2, and 35.4 units simulation time
   * }}}
   */
  final def observeAt(observationTimes: Double*) = {
    times = Some(observationTimes.toList)
  }

  /**
   * Observe at a specific range of times.
   *  @param r a [[ValueRange]]
   *  @example {{{
   *  	observeAt(range(10,5,100)) // Triggers observation after 10, 15, ..., 95, 100 units simulation time
   *  }}}
   */
  final def observeAt[T <: AnyVal](r: ValueRange[T])(implicit n: Numeric[T]) = {
    timeRange = Some(ValueRange[Double](n.toDouble(r.from), n.toDouble(r.step), n.toDouble(r.to)))
  }

  /**
   * Defines what to observe by 'binding' a variable name to a model/simulation-specific ('internal') variable.
   * In the most simple case, the name to be used in SESSL is the same as the internal name.
   * @param binds a list of [[DataElemBinding]] (typically defined implicitly)
   * @example {{{
   * 	observe("A", "B" ~ "C", "D" to "E") // Observes 'A', 'C', and 'E' (with 'C' and 'E' being bound to names 'B' and 'D', respecitvely)
   * }}}
   */
  def observe(binds: DataElemBinding*) = {
    for (binding <- binds) {
      val boundSesslNames = bindings.getOrElse(binding.internalName, Set()) += binding.sesslName
      bindings(binding.internalName) = boundSesslNames
    }
  }

  /**
   * Add result handler that processes observed model output from a single run. The result is an [[ObservationRunResultsAspect]].
   *  @param f result handler
   *  @example {{{
   *  	withRunResult { result =>
   *   		println(result)
   *    }
   *  }}}
   */
  def withRunResult(f: ObservationRunResultsAspect => Unit) = {
    afterRun {
      (
        r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractObservation]).get.asInstanceOf[ObservationRunResultsAspect]))
    }
  }

  /**
   * Add result handler that processes observed model output from a set of replications.
   * The result is an [[ObservationReplicationsResultsAspect]].
   * @param f result handler
   * @example {{{
   *  	withReplicationsResult { result=>
   *   		println(result)
   *    }
   *  }}}
   */
  def withReplicationsResult(f: ObservationReplicationsResultsAspect => Unit) = {
    afterReplications {
      (
        r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractObservation]).get.asInstanceOf[ObservationReplicationsResultsAspect]))
    }
  }

  /**
   * Add result handler that processes observed model output from the whole experiment.
   *  The result is an [[ObservationExperimentResultsAspect]].
   * @param f result handler
   * @example {{{
   *  	withExperimentResult { result =>
   *   		println(result)
   *    }
   *  }}}
   */
  def withExperimentResult(f: ObservationExperimentResultsAspect => Unit) = {
    afterExperiment {
      (
        r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractObservation]).get.asInstanceOf[ObservationExperimentResultsAspect]))
    }
  }

  /** All simulation time points at which the values of observed variables shall be stored. */
  lazy val observationTimes: List[Double] = {
    val unsortedTimes: List[Double] = {
      if (times.isDefined)
        times.get
      else if (timeRange.isDefined)
        timeRange.get.toList
      else {
        logger.warn("Neither specific times nor a time range is given for observation.")
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

  /** Before the experiment is done, add result aspect for observation. */
  override def collectExperimentResultsAspects() {
    super.collectExperimentResultsAspects()
    addExperimentResultsAspect(new ObservationExperimentResultsAspect())
  }

  /**
   * Collects the results of the indicated run. If the removeData flag is set to true,
   *  the observation sub-system may regard the data as read-out (and hence delete it).
   *
   *  @param runID the ID of the run
   *  @param removeData flag to signal that the data will not be required again (and can hence be dismissed)
   *  @return the result aspect of the run (w.r.t. observation)
   */
  def collectResults(runID: Int, removeData: Boolean): ObservationRunResultsAspect

  /**
   * Signals that all results for the given configuration ID have been collected.
   *  Override to clean up auxiliary data structures.
   */
  def collectReplicationsResults(assignID: Int): ObservationReplicationsResultsAspect

}

/**
 * Represents an association between a variable name in SESSL and a model/simulator-specific ('internal') variable.
 *  @param sesslName the name to be used in SESSL
 *  @param internalName the internal name
 *  @example {{{
 * val x: DataElemBinding = "X" //Equals "X" ~ "X"
 * val y: DataElemBinding = "X" ~ "X"
 * val z: DataElemBinding = "X" to "Y"
 *  }}}
 */
sealed class DataElemBinding(val sesslName: String, val internalName: String) {
  /** Simple constructor for "X"~"X" bindings. */
  def this(singleName: String) = this(singleName, singleName)
}

/**
 * Name of a data element, first part of a [[DataElemBinding]].
 *  @param sesslName the name to be used in SESSL
 */
final case class DataElemName(override val sesslName: String) extends DataElemBinding(sesslName, sesslName) {
  /**
   * Binds SESSL name to internal name.
   *  @param internalName the internal name
   *  @return the corresponding [[DataElemBinding]]
   */
  def to(internalName: String) = new DataElemBinding(sesslName, internalName)

  /** Equal to <code>"X" to "Y"</code>. */
  def ~(internalName: String) = to(internalName)
}

/**
 * The [[RunResultsAspect]] for [[AbstractObservation]]. Additional methods to work on values are provided by trait [[util.ResultOperations]].
 *
 *  @param data the data recorded for a single run: variable name (in SESSL) => trajectory.
 */
class ObservationRunResultsAspect(private var data: Map[String, Trajectory]) extends RunResultsAspect(classOf[AbstractObservation]) with ResultOperations {

  /** Auxiliary constructor to merge two result sets (e.g. recorded by different entities but for the same run).*/
  def this(aspects: (ObservationRunResultsAspect, ObservationRunResultsAspect)) = {
    this(aspects._1.data ++ aspects._2.data)
  }

  /**
   * Get the _last_ recorded value of a variable.
   *  @param name variable name
   *  @example {{{
   *  	withRunResult { result =>
   *   		println("last value: " + result("x"))
   *    }
   *  }}}
   */
  def apply(name: String): Any = {
    val values = getVarData(name)
    require(!values.isEmpty, "No values stored for variable '" + name + "'!")
    values.last._2
  }

  /**
   * Checks whether a variable name is defined in the results.
   *  @param name variable name
   *  @return true if there is data stored for this variable
   *  @example {{{
   *  	withRunResult { result =>
   *   		if(result ? "x") { ... }
   *    }
   *  }}}
   */
  def ?(name: String): Boolean = data.contains(name)

  /**
   * Get [[Trajectory]] for a given variable name.
   *  @param name variable name
   *  @return trajectory
   *  @example {{{
   *  	withRunResult { result =>
   *   		println(result.trajectory("x"))
   *    }
   *  }}}
   */
  def trajectory(name: String): Trajectory = getVarData(name)

  /**
   * Get tuple of variable name and [[Trajectory]].
   *  @param name variable name
   *  @return tuple of variable name and [[Trajectory]]
   *  @example {{{
   *  	withRunResult { result =>
   *      reportSection("Example") {
   *   		linePlot(result ~ ("x"))(title = "Example line plot")
   *     }
   *    }
   *  }}}
   */
  def ~(name: String): (String, Trajectory) = (name, getVarData(name))

  /**
   * Get all data in a manner that is easy to plot.
   *  @return sequence with tuples of variable name and corresponding [[Trajectory]]
   *  @example {{{
   *  	withRunResult { result =>
   *      reportSection("Example") {
   *   		linePlot(result.all)(title = "Line plot for all variables")
   *     }
   *    }
   *  }}}
   */
  def all: Iterable[(String, Trajectory)] = names.map(this ~ _)

  /**
   * Get all times at which the data was observed.
   *  @param name variable name
   *  @return all time stamps for which data is available
   */
  def times(name: String): Iterable[Double] = getVarData(name).map(_._1)

  /**
   * Get all values that have been observed.
   *  @param name variable name
   *  @return all values
   */
  def values(name: String): Iterable[_] = getVarData(name).map(_._2)

  /**
   * Get the names of all defined variables.
   *  @return all variable names
   */
  lazy val names = data.keys.toList

  /** Checks whether variable is defined and returns it if possible. */
  private def getVarData(varName: String): Trajectory = {
    require(this ? varName, "Variable '" + varName + "' is not defined!")
    data.get(varName).get
  }

  override protected def getValuesFor(name: String) = values(name)
}

/** The [[ReplicationsResultsAspect]] for [[AbstractObservation]]. */
class ObservationReplicationsResultsAspect extends ReplicationsResultsAspect(classOf[AbstractObservation]) with ResultOperations {

  /**
   * Get the _last_ recorded value of the specified variable for all runs.
   *  @param name variable name
   *  @return the last recorded value for each run
   */
  def apply(name: String): Iterable[Any] = {
    runsResults.mapValues(_.asInstanceOf[ObservationRunResultsAspect](name)).values
  }

  /**
   * Apply name to the result, combine results in _named_ tuple.
   *  @param name variable name
   *  @return tuple with variable name and all last observed results
   *  @example {{{
   *  withReplicationsResult { result =>
   *  	reportSection("Sample Histogram") {
   * 		histogram(result ~ ("x"))(title = "Last observed values of 'x'")
   * 	}
   *  }}}
   */
  def ~(name: String) = (name, apply(name))

  override protected def getValuesFor(name: String) = apply(name)
}

/**
 * [[ExperimentResultsAspect]] for [[AbstractObservation]]. Additional methods are provided by [[util.ResultOperations]] and [[PartialExperimentResults]].
 */
class ObservationExperimentResultsAspect extends ExperimentResultsAspect(classOf[AbstractObservation]) with ResultOperations
  with PartialExperimentResults[ObservationExperimentResultsAspect] {

  /**
   * Get the _last_ recorded value of the specified variable for all runs.
   *  @param name variable name
   *  @return the last recorded value for each run
   */
  def apply(name: String) = runsResults.mapValues(_.asInstanceOf[ObservationRunResultsAspect](name)).values.toList

  /**
   * Apply name to the result, combine results in _named_ tuple.
   *  @param name variable name
   *  @return tuple with variable name and all last observed results
   *  @example {{{
   *  withExperimentResult { result =>
   *  	reportSection("Sample Histogram") {
   * 		histogram(result ~ ("x"))(title = "Last observed values of 'x'")
   * 	}
   *  }}}
   */
  def ~(name: String) = (name, apply(name))

  override protected def getValuesFor(name: String) = apply(name)

  override def createPartialResult(runsResults: Map[Int, RunResultsAspect], replicationsResults: Map[Int, ReplicationsResultsAspect]): ObservationExperimentResultsAspect = {
    val aspect = new ObservationExperimentResultsAspect
    aspect.setResults(runsResults, replicationsResults)
    aspect
  }
}
