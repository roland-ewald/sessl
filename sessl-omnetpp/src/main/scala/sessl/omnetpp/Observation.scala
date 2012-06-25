package sessl.omnetpp

import java.io.File
import sessl.util.SimpleObservation
import sessl.Duration

/**
 * Result observation support for OMNeT++.
 *
 * Limitations:
 * - only numeric observations are supported
 * - all recording modes and intervals are activated for each variable to be observed
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
    if (observationTimes.isEmpty) {
      //TODO: use logging
      println("Warning: to observation times defined, no vector data will be observed.")
      configureRecording(allEntitiesPattern, true, false)
    } else {
      configureRecording(allEntitiesPattern, false, false)
      variableBindings.keys.foreach {
        varName =>
          configureRecording(varName, true, true)
          defineRecordedData(varName)
      }
      require(warmUpPhase.asSecondsOrUnitless <= observationTimes.head,
        "Warm-up phase " + warmUpPhase + " overlaps with smallest observation time, " + observationTimes.head + ".")
    }
  }

  /** (De-)activate scalar recording for a given variable name. */
  private[this] def configureRecording(varNamePattern: String, scalarsEnabled: Boolean, vectorsEnabled: Boolean) = {
    write(varNamePattern + ".scalar-recording", scalarsEnabled.toString)
    write(varNamePattern + ".vector-recording", vectorsEnabled.toString)
  }

  /** Activates all recording modes over the whole simulation interval. */
  private[this] def defineRecordedData(varNamePattern: String) = {
    write(varNamePattern + ".result-recording-modes", "all")
    write(varNamePattern + ".vector-recording-intervals", "0..")
  }

  abstract override def considerResults(runId: Int, workingDir: File) = {
    super.considerResults(runId, workingDir)
    println("I'm considering run " + runId)

    for (observationTime <- observationTimes) {

    }

    //TODO Add options: 
    //warmup-period = 20s

    //TODO: Map from observerAt(...) elements to this
    // -wildcards: a mechanism is required to 'resolve' wildcards, i.e. to register new matching variables at runtime (internal = external name) 
    //  (for this AbstractObservation#bindings needs to be updated)
  }

}