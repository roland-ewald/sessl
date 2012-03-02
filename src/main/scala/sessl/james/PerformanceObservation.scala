package sessl.james

import sessl.AbstractPerformanceObservation
import sessl.AbstractExperiment
import sessl.SupportSimulatorConfiguration
import sessl.PerfObsRunResultsAspect
import sessl.PerfObsRunResultsAspect

/** Support for performance observation in James II.
 *  @author Roland Ewald
 */
trait PerformanceObservation extends AbstractPerformanceObservation {
  this: AbstractExperiment with SupportSimulatorConfiguration =>

  override def collectResults(runID: Int, removeData: Boolean): PerfObsRunResultsAspect =
    new PerfObsRunResultsAspect(NextReactionMethod(), 1.0) //TODO
}