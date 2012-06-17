package sessl.omnetpp

import sessl.AbstractObservation
import sessl.ObservationReplicationsResultsAspect
import sessl.ObservationRunResultsAspect

/**
 * Result observation support in SESSL.
 *
 * @author Roland Ewald
 *
 */
trait Observation extends AbstractObservation {
  this: Experiment =>

  def collectResults(runID: Int, removeData: Boolean): ObservationRunResultsAspect = { null }

  def collectReplicationsResults(assignID: Int): ObservationReplicationsResultsAspect = { null }

}