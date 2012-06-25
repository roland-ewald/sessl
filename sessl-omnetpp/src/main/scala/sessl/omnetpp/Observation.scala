package sessl.omnetpp

import java.io.File
import sessl.util.SimpleObservation
import sessl.Duration

/**
 * Result observation support for OMNeT++.
 *
 *  @author Roland Ewald
 *
 */
trait Observation extends SimpleObservation with OMNeTPPResultHandler {
  this: Experiment =>

  /** The warm-up phase. */
  var warmUpPhase = Duration()

  /** Name pattern that matches all entities. */
  val allEntitiesPattern = "**"

  override def configure(): Unit = {
    super.configure()
    writeComment("Observation Configuration")
    defineScalarRecording(allEntitiesPattern, false)
    variableBindings.keys.foreach {
      varName =>
        activateAllRecordingModes(varName)
        defineScalarRecording(varName, true)
    }
  }

  /** (De-)activate scalar recording for a given variable name. */
  private[this] def defineScalarRecording(varNamePattern: String, enabled: Boolean) = write(varNamePattern + ".scalar-recording", enabled.toString)

  /** Activates all recording modes. */
  private[this] def activateAllRecordingModes(varNamePattern: String) = write(varNamePattern + ".result-recording-modes", "all")

  abstract override def considerResults(runId: Int, workingDir: File) = {
    super.considerResults(runId, workingDir)
    println("I'm considering run " + runId)

    for (observationTime <- observationTimes) {

    }

    //TODO Add options: 
    //warmup-period = 20s

    //TODO: Map from observerAt(...) elements to this

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