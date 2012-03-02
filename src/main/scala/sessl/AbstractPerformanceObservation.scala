package sessl

/** Support for performance observation and storage.
 *  @author Roland Ewald
 */
trait AbstractPerformanceObservation extends ExperimentConfiguration {
  this: AbstractExperiment with SupportSimulatorConfiguration =>

  /** Specifies where to store the data. */
  private[this] var performanceDataSinkSpec: Option[PerformanceDataSinkSpecification] = None
  
  val experimentPerformances = new ExperimentPerformance()

  def withRunPerformance(f: RunPerformance => Unit) = afterRun(_ => f(new RunPerformance()))

  def withReplicationsPerformance(f: ReplicationsPerformance => Unit) = afterReplications(_ => f(new ReplicationsPerformance()))

  def withExperimentPerformance(f: ExperimentPerformance => Unit) = afterExperiment(_ => f(experimentPerformances))

}

trait PerformanceDataSinkSpecification

trait DatabasePerformanceSink {
  
}

trait PerformanceDomain

class RunPerformance

class ReplicationsPerformance

class ExperimentPerformance