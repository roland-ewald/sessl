package sessl.omnetpp

import sessl.AbstractObservation
import sessl.ObservationReplicationsResultsAspect
import sessl.ObservationRunResultsAspect
import sessl.util.SimpleObservation
import java.io.File

/**
 * Result observation support for OMNeT++.
 *
 * @author Roland Ewald
 *
 */
trait Observation extends SimpleObservation with OMNeTPPResultHandler {
  this: Experiment =>

  abstract override def considerResults(runId: Int, workingDir: File) = {
    super.considerResults(runId, workingDir)
    println("I'm considering run " + runId)

  }

}