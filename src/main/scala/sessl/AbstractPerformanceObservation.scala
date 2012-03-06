package sessl
import scala.annotation.implicitNotFound

import sessl.util.AlgorithmSet
import sessl.util.MiscUtils

/** Support for performance observation and storage.
 *  @author Roland Ewald
 */
trait AbstractPerformanceObservation extends ExperimentConfiguration {
  this: AbstractExperiment with SupportSimulatorConfiguration =>

  /** Specifies where to store the data. */
  private[this] var performanceDataSinkSpec: Option[PerformanceDataSinkSpecification] = None

  /** Adds event handler to analyze the performance of a single run. */
  def withRunPerformance(f: PerfObsRunResultsAspect => Unit) = {
    afterRun {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsRunResultsAspect])
    }
  }

  /** Adds event handler to analyze the performance for a set of replications. */
  def withReplicationsPerformance(f: PerfObsReplicationsResultsAspect => Unit) = {
    afterReplications {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsReplicationsResultsAspect])
    }
  }

  /** Adds event handler to analyze the performance for the whole experiment. */
  def withExperimentPerformance(f: PerfObsExperimentResultsAspect => Unit) = {
    afterExperiment {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).get.asInstanceOf[PerfObsExperimentResultsAspect])
    }
  }

  /** Before the run is done, add the performance data on this run to the experiment. */
  override def collectRunResultsAspects(runId: Int) {
    super.collectRunResultsAspects(runId)
    addRunResultsAspect(runId, collectResults(runId, true))
  }

  /** Before the replications are done, add all results of this run to the experiment. */
  override def collectReplicationsResultsAspects(assignId: Int) {
    super.collectReplicationsResultsAspects(assignId)
    addReplicationsResultsAspect(assignId, new PerfObsReplicationsResultsAspect())
  }

  /** Before the experiment is done, add result aspect for instrumentation. */
  override def collectExperimentResultsAspects() {
    super.collectExperimentResultsAspects()
    addExperimentResultsAspect(new PerfObsExperimentResultsAspect())
  }

  /** Collects the performance results of the indicated run. If the removeData flag is set to true,
   *  the performance observation sub-system may regard the data as read-out (and hence delete it).
   *
   *  @param runID the ID of the run
   *  @param removeData flag to signal that the data will not be required again (and can hence be dismissed)
   *  @return the result aspect of the run (w.r.t. performance)
   */
  def collectResults(runID: Int, removeData: Boolean): PerfObsRunResultsAspect

}

/** Super type of all performance data sink specifications. */
trait PerformanceDataSinkSpecification

/** Database performance data sinks. */
case class PerformanceDatabaseDataSink(url: String = "not://specified", user: String = "username", password: String = "", driver: String = "unknown driver")
  extends DataSinkSpecification

/** Provides operations for aggregated performance results (collecting all run times, filtering by setups). */
trait AggregatedPerformanceOperations[T <: { def runsResultsMap: Map[Int, RunResultsAspect] }] {
  this: T =>

  /** Retrieves all run times for a set of results. */
  def runtimes =
    retrieveRuntimes(runsResultsMap)

  /** The set of all setups, each used by at least one run. */
  lazy val allSetups = runsResultsMap.mapValues(_.asInstanceOf[PerfObsRunResultsAspect].setup).values.toSet

  /** Retrieves all run times for a set of results executed with certain setups. */
  def runtimes(algorithms: Any): Iterable[Double] = {
    algorithms match {
      case algo: Simulator => runtimesFor(AlgorithmSet[Simulator](algo))
      case algoSeq: Seq[_] => algoSeq.head match {
        case s: Simulator => runtimesFor(AlgorithmSet[Simulator](algoSeq.asInstanceOf[Seq[Simulator]]))
        case x => throw new IllegalArgumentException("Object '" + algoSeq.head + "' in sequence '" + algoSeq + "' not supported for run times lookup.")
      }
      case x => throw new IllegalArgumentException("Object '" + algorithms + "' not supported for run times lookup.")
    }
  }

  /** Retrieve the run times for all setups, sorted alphabetically by the string representation of the setup. */
  def runtimesAllSetups: Seq[(String, List[Double])] = runtimesSomeSetups(allSetups.toSeq)

  /** Retrieve the run times for some setups, sorted alphabetically by the string representation of the setup. */
  def runtimesSomeSetups(setups: Seq[Simulator]): Seq[(String, List[Double])] =
    setups.map(setup => (setup.toString, runtimesFor(AlgorithmSet[Simulator](setup)).toList)).toSeq.sortBy(_._1)

  /** Retrieve runtime results for all runs having used a setup that is contained in the given set. */
  private[this] def runtimesFor(setups: AlgorithmSet[Simulator]) =
    retrieveRuntimes(runsResultsMap.filter(entry => setups.algorithmSet(entry._2.asInstanceOf[PerfObsRunResultsAspect].setup)))

  /** Retrieve the runtime results for a given number of runs, represented by a map run id => run result aspect. */
  private[this] def retrieveRuntimes(results: Map[Int, RunResultsAspect]) =
    results.values.map(_.asInstanceOf[PerfObsRunResultsAspect].runtime)
}

/** The performance aspects of a single simulation run. */
class PerfObsRunResultsAspect(val setup: Simulator, val runtime: Double) extends RunResultsAspect(classOf[AbstractPerformanceObservation])

/** The performance aspects of a set of simulation runs, all computing the same variable assignment. */
class PerfObsReplicationsResultsAspect extends ReplicationsResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsReplicationsResultsAspect]

/** The performance aspects of all simulation runs executed during the experiment. */
class PerfObsExperimentResultsAspect extends ExperimentResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsExperimentResultsAspect]

