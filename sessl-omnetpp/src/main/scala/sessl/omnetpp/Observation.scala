package sessl.omnetpp

import java.io.File
import sessl.util.SimpleObservation
import sessl.Duration

/** Result observation support for OMNeT++.
 *
 *  Limitations:
 *  - only numeric observations are supported
 *  - all recording modes and intervals are activated for each variable to be observed
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
      configureWarmUpPhase()
    }
  }

  /** Configures warm-up phase. */
  def configureWarmUpPhase() = {
    require(warmUpPhase.asSecondsOrUnitless <= observationTimes.head,
      "Warm-up phase " + warmUpPhase + " overlaps with smallest observation time, " + observationTimes.head + ".")
    write("warmup-period", if (warmUpPhase.time > 0) warmUpPhase.time.toString else warmUpPhase.toSeconds + "s")
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

    val currentBindings = variableBindings

    //Read in vector data
    ResultReader.readVectorFile(workingDir.toString(), runId).values.foreach {
      vectorData =>
        {
          val vectorName = vectorData._1.name
          //If the vector is recorded but not found in the bindings, a wildcard pattern was used and
          // the matched name should be added
          if (!currentBindings.contains(vectorName)) {
            observe(vectorName)
          }
          addVectorValuesFor(runId, vectorName, vectorData._2)
        }
    }

    //TODO: scalars
    ResultReader.readScalarFile(workingDir.toString(), runId).values.foreach {
      scalarData =>
        {
          val x = scalarData
        }
    }
  }

  private def observeIfNecessary(registeredNames: Set[String], varName: String) = {

  }

  def addVectorValuesFor(runId: Int, internalName: String, recordedData: List[VectorDataEntry]) {
    //TODO
    //Read vector data:
    for (observationTime <- observationTimes) {
    }
    //addValueFor(runId)          
  }

}