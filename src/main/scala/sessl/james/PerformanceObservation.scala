package sessl.james

import sessl.AbstractPerformanceObservation
import sessl.AbstractExperiment
import sessl.SupportSimulatorConfiguration

/**
 * Support for performance observation in James II.
 * @author Roland Ewald
 */
trait PerformanceObservation extends AbstractPerformanceObservation {
  this: AbstractExperiment with SupportSimulatorConfiguration =>
}