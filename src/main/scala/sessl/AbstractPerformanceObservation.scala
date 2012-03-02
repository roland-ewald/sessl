package sessl
import util.AlgorithmSet
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
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).asInstanceOf[PerfObsRunResultsAspect])
    }
  }

  /** Adds event handler to analyze the performance for a set of replications. */
  def withReplicationsPerformance(f: PerfObsReplicationsResultsAspect => Unit) = {
    afterReplications {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).asInstanceOf[PerfObsReplicationsResultsAspect])
    }
  }

  /** Adds event handler to analyze the performance for the whole experiment. */
  def withExperimentPerformance(f: PerfObsExperimentResultsAspect => Unit) = {
    afterExperiment {
      r => MiscUtils.saveApply(f, r.aspectFor(classOf[AbstractPerformanceObservation]).asInstanceOf[PerfObsExperimentResultsAspect])
    }
  }

}

//TODO: define kinds of performance data sinks
trait PerformanceDataSinkSpecification
trait DatabasePerformanceSink

/** Provides operations for aggregated performance results (collecting all run times, filtering by setups). */
trait AggregatedPerformanceOperations[T <: { def runsResultsMap: Map[Int, RunResultsAspect] }] {
  this: T =>

  /** Retrieves all run times for a set of results. */
  def runtimes =
    retrieveRuntimes(runsResultsMap)

  /** Retrieves all run times for a set of results executed with certain setups. */
  def runtimesFor(setups: AlgorithmSet[Simulator]) =
    retrieveRuntimes(runsResultsMap.filter(entry => setups.algorithmSet(entry._2.asInstanceOf[PerfObsRunResultsAspect].setup)))

  private[this] def retrieveRuntimes(results: Map[Int, RunResultsAspect]) =
    results.values.map(_.asInstanceOf[PerfObsRunResultsAspect].runtime)
}

/** The performance aspects of a single simulation run. */
class PerfObsRunResultsAspect(val setup: Simulator, val runtime: Int) extends RunResultsAspect(classOf[AbstractPerformanceObservation])

/** The performance aspects of a set of simulation runs, all computing the same variable assignment. */
class PerfObsReplicationsResultsAspect extends ReplicationsResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsReplicationsResultsAspect]

/** The performance aspects of all simulation runs executed during the experiment. */
class PerfObsExperimentResultsAspect extends ExperimentResultsAspect(classOf[AbstractPerformanceObservation])
  with AggregatedPerformanceOperations[PerfObsExperimentResultsAspect]



