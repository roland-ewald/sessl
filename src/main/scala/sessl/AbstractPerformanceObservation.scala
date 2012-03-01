package sessl

/** Support for performance observation and storage.
 *  @author Roland Ewald
 */
trait AbstractPerformanceObservation extends ExperimentConfiguration {
  this: Experiment with SupportSimulatorConfiguration =>

  private[this] var performanceDataSinkSpec: Option[PerformanceDataSinkSpecification] = None
  
  val experimentPerformances = new ExperimentPerformance()

  def performanceAfterRun(f: RunPerformance => Unit) = afterRun(_ => f(new RunPerformance()))

  def performanceAfterReplications(f: ReplicationsPerformance => Unit) = afterReplications(_ => f(new ReplicationsPerformance()))

  def performanceAfterExperiment(f: ExperimentPerformance => Unit) = afterExperiment(_ => f(experimentPerformances))

}

trait PerformanceDataSinkSpecification

case class DatabasePerformanceSink()

trait PerformanceDomain

class RunPerformance

class ReplicationsPerformance

class ExperimentPerformance