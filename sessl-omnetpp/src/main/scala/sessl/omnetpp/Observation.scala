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

    //TODO Add options: 
    //warmup-period = 20s
    //output-vector-file = "${resultdir}/${configname}-${runnumber}.vec"
    //output-scalar-file = "${resultdir}/${configname}-${runnumber}.sca"

    //TODO: Map from observerAt(...) elements to this

    //**.result-recording-modes = all
    //recordingModes {
    // "**" ~> All
    // "x" ~> None
    // "y" ~> Default
    // "z" ~> Without("u") and With("v")
    //}
    //**.scalar-recording = false

    // scalarRecording {
    //   "**" ~> Disabled
    //   "x" ~> Enabled
    // }

    //same for vectorRecording {...}
    //+ Record intervals: **.vector-recording-intervals = 0..
    //+ **.vector-record-eventnumbers = false

    //Some notes
    // -defenisve: by default, everything should be disabled
    // -limited: non-numeric observations not supported by sessl so far
    // -wildcards: a mechanism is required to 'resolve' wildcards, i.e. to register new matching variables at runtime (internal = external name) 
    //  (for this AbstractObservation#bindings needs to be updated)
  }

}