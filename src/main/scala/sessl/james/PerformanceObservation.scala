package sessl.james

import sessl.AbstractPerformanceObservation
import sessl.Experiment
import sessl.SupportSimulatorConfiguration

/**
 * Support for performance observation in James II.
 * @author Roland Ewald
 */
trait PerformanceObservation extends AbstractPerformanceObservation {
  this: Experiment with SupportSimulatorConfiguration =>
}