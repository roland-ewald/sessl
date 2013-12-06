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

import sessl.util.AlgorithmSet
import sessl.util.MiscUtils

/**
 * Support for performance observation and performance measurement storage.
 *
 * @example {{{
 * new Experiment with PerformanceObservation {
 * 	//...
 * }
 * }}}
 *
 *  @author Roland Ewald
 */
trait AbstractPerformanceObservation extends ExperimentConfiguration {
  this: AbstractExperiment with SupportSimulatorConfiguration =>

  /** Specifies where to store the data. */
  protected[sessl] var performanceDataSinkSpecication: Option[PerformanceDataSinkSpecification] = None

  /** Set the [[PerformanceDataSinkSpecification]]. */
  def performanceDataSink_=(pds: PerformanceDataSinkSpecification) = { performanceDataSinkSpecication = Some(pds) }
  
  /** Get the [[PerformanceDataSinkSpecification]]. */
  def performanceDataSink: PerformanceDataSinkSpecification = { performanceDataSinkSpecication.get }

  /**
   * Handler performance results of a single run. The result is a [[PerfObsRunResultsAspect]].
   *  @example {{{
   *  	withRunPerformance { result =>
   *   		println(result)
   *    }
   *  }}}
   */
  def withRunPerformance(f: PerfObsRunResultsAspect => Unit) = {
    afterRun {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsRunResultsAspect])
    }
  }

  /**
   * Handle performance results of a set of replications. The result is a [[PerfObsReplicationsResultsAspect]].
   * @example {{{
   *  	withReplicationsPerformance { result =>
   *   		println(result)
   *    }
   *  }}}
   */
  def withReplicationsPerformance(f: PerfObsReplicationsResultsAspect => Unit) = {
    afterReplications {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsReplicationsResultsAspect])
    }
  }

  /**
   * Handle performance results for the whole experiment. The result is a [[PerfObsExperimentResultsAspect]].
   * @example {{{
   *  	withExperimentPerformance { result =>
   *   		println(result)
   *    }
   *  }}}
   */
  def withExperimentPerformance(f: PerfObsExperimentResultsAspect => Unit) = {
    afterExperiment {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsExperimentResultsAspect])
    }
  }

  /** Before the run is done, add the performance data on this run to the experiment. */
  override def collectRunResultsAspects(runId: Int) {
    super.collectRunResultsAspects(runId)
    addRunResultsAspect(runId, collectPerformanceResults(runId, true))
  }

  /** Before the replications are done, add all results of this run to the experiment. */
  override def collectReplicationsResultsAspects(assignId: Int) {
    super.collectReplicationsResultsAspects(assignId)
    addReplicationsResultsAspect(assignId, new PerfObsReplicationsResultsAspect())
  }

  /** Before the experiment is done, add result aspect for performance observation. */
  override def collectExperimentResultsAspects() {
    super.collectExperimentResultsAspects()
    addExperimentResultsAspect(new PerfObsExperimentResultsAspect())
  }

  /**
   * Collects the performance results of the indicated run. If the removeData flag is set to true,
   *  the performance observation sub-system may regard the data as read-out (and hence delete it).
   *
   *  @param runID the ID of the run
   *  @param removeData flag to signal that the data will not be required again (and can hence be dismissed)
   *  @return the result aspect of the run (w.r.t. performance)
   */
  def collectPerformanceResults(runID: Int, removeData: Boolean): PerfObsRunResultsAspect

}

/** Super type of all performance data sink specifications. */
trait PerformanceDataSinkSpecification

/**
 * Database performance data sinks.
 *  @param url the database URL
 *  @param user the user name (default is empty)
 *  @param password the password (default is empty)
 *  @param driver the class name of the JDBC driver (default is 'com.mysql.jdbc.Driver')
 */
case class PerformanceDatabaseDataSink(url: String, user: String = "", password: String = "", driver: String = "com.mysql.jdbc.Driver")
  extends DataSinkSpecification

/** Provides operations for aggregated performance results (collecting all run times, filtering by setups). */
trait AggregatedPerformanceOperations[T <: { def runsResultsMap: Map[Int, RunResultsAspect] }] {
  this: T =>

  /** Retrieves all run times. */
  def runtimes =
    retrieveRuntimes(runsResultsMap)

  /** The set of all setups, each used by at least one run. */
  lazy val allSetups = runsResultsMap.mapValues(_.asInstanceOf[PerfObsRunResultsAspect].setup).values.toSet

  /**
   * Retrieves all run times for a set of results executed with certain setups.
   *  @param setups single simulator setup (or sequence thereof)
   *  @return run times
   */
  def runtimes(setups: Any): Iterable[Double] = runtimesFor(retrieveAlgorithmSet(setups))

  /**
   * Retrieve the run times for all setups, sorted alphabetically by the string representation of the setup.
   * @return tuples (setup name, list of run times)
   */
  def runtimesForAll: Seq[(String, Iterable[Double])] = runtimesFor(allSetups.toSeq)

  /**
   * Retrieve run times for single setup.
   *  @param s the the simulation setup
   *  @return one-element sequence containing a tuple (setup name, list of run times)
   */
  def runtimesFor(setup: Simulator): Seq[(String, Iterable[Double])] = runtimesFor(Seq(setup))

  /**
   * Retrieve the run times for some setups, sorted alphabetically by the string representation of the setup.
   *  @param setups the simulation setups of interest
   *  @return tuples (setup name, list of run times)
   */
  def runtimesFor(setups: Seq[Simulator]): Seq[(String, Iterable[Double])] =
    setups.map(setup => (setup.toString, runtimesFor(AlgorithmSet[Simulator](setup)))).sortBy(_._1)

  /**
   * Gets general results for certain setups.
   *  @param setups the simulation setups of interest
   *  @return result aspect restricted to those setups
   */
  def forSetups(setups: Any) =
    filterBySetups(retrieveAlgorithmSet(setups)).map(_._2.results)

  /** Retrieve runtime results for all runs having used a setup that is contained in the given set. */
  private[this] def runtimesFor(setups: AlgorithmSet[Simulator]) = retrieveRuntimes(filterBySetups(setups))

  /** Retrieve the runtime results for a given number of runs, represented by a map run id => run result aspect. */
  private[this] def retrieveRuntimes(results: Map[Int, RunResultsAspect]) =
    results.values.map(_.asInstanceOf[PerfObsRunResultsAspect].runtime)

  /** Filter the results by setups. */
  private[this] def filterBySetups(setups: AlgorithmSet[Simulator]) =
    runsResultsMap.filter(entry => setups.algorithmSet(entry._2.asInstanceOf[PerfObsRunResultsAspect].setup))

  /** Checks input if it is a single simulation algorithm or a sequence of them. */
  private[this] def retrieveAlgorithmSet(algorithms: Any): AlgorithmSet[Simulator] = algorithms match {
    case algo: Simulator => AlgorithmSet[Simulator](algo)
    case algoSeq: Seq[_] => algoSeq.head match {
      case s: Simulator => AlgorithmSet[Simulator](algoSeq.asInstanceOf[Seq[Simulator]])
      case x => throw new IllegalArgumentException("Object '" + algoSeq.head + "' in sequence '" + algoSeq + "' is not a simulator.")
    }
    case x => throw new IllegalArgumentException("Object '" + algorithms + "' is not a simulator.")
  }
}

/**
 * The performance aspects of a single simulation run.
 *  @param setup the simulation algorithm
 *  @param runtime the execution time of this run
 */
class PerfObsRunResultsAspect(val setup: Simulator, val runtime: Double) extends RunResultsAspect(classOf[AbstractPerformanceObservation]) {
  /** Method to request a certain performance metric. If possible, use Scala 2.10's reflection API here in future. */
  def apply(name: String) = name match {
    case "runtime" => runtime
    case x => throw new IllegalArgumentException("Metric '" + x + "' is not supported")
  }
}

/** The performance aspects of a set of simulation runs, all computing the same variable assignment. Implements [[AggregatedPerformanceOperations]].*/
class PerfObsReplicationsResultsAspect extends ReplicationsResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsReplicationsResultsAspect] {

  /** Add all results observed for the given algorithms to a given replication results aspect. */
  def forSetupsAndAspect[R <: ReplicationsResultsAspect](algorithms: Any, aspect: R): R = {
    val repResults = new ReplicationsResults(this.results.id)
    for (runResult <- forSetups(algorithms)) {
      repResults += runResult
    }
    repResults.addAspect(aspect)
    aspect
  }
}

/**
 * The performance aspects of all simulation runs executed during the experiment. Implements [[AggregatedPerformanceOperations]] and [[PartialExperimentResults]].
 *  @example {{{
 *  withExperimentPerformance { result =>
 *    reportSection("Result") {
 *      histogram(result.having("x" <~ 1).runtimes)(title="All simulator runtimes for those replication sets where model parameter 'x' is assigned to 1")
 *    }
 *  }
 *  }}}
 */
class PerfObsExperimentResultsAspect extends ExperimentResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsExperimentResultsAspect] with PartialExperimentResults[PerfObsExperimentResultsAspect] {

  /**
   * Get the last sample for the given variable from all runs.
   * @param name result name (for example, 'runtime')
   */
  def apply(name: String) = runsResults.mapValues(_.asInstanceOf[PerfObsRunResultsAspect](name)).values.toList

  /**
   * Apply name to the result, combine results in _named_ tuple.
   *  @param name result name (for example, 'runtime')
   */
  def ~(name: String) = (name, apply(name))

  override protected def getValuesFor(name: String) = apply(name)

  override def createPartialResult(runsResults: scala.collection.mutable.Map[Int, RunResultsAspect],
    replicationsResults: scala.collection.mutable.Map[Int, ReplicationsResultsAspect]): PerfObsExperimentResultsAspect = {
    val aspect = new PerfObsExperimentResultsAspect
    aspect.setResults(runsResults, replicationsResults)
    aspect
  }
}

