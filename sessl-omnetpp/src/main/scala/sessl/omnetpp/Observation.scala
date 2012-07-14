/*******************************************************************************
 * Copyright 2012 Roland Ewald
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package sessl.omnetpp

import java.io.File
import sessl.util.SimpleObservation
import sessl.Duration
import sessl.util.Interpolation
import scala.collection.mutable.ListBuffer

/**
 * Result observation support for OMNeT++.
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
  private[this] var warmUpPhase: Option[Duration] = None

  /** Name pattern that matches all entities. */
  val allEntitiesPattern = "**"

  override def configure(): Unit = {
    super.configure()
    writeComment("Observation Configuration")
    if (observationTimes.isEmpty) {
      logger.warn("Warning: to observation times defined, no vector data will be observed.")
      configureRecording(allEntitiesPattern, true, false)
    } else {
      variableBindings.keys.foreach {
        varName =>
          configureRecording(varName, true, true)
          defineRecordedData(varName)
      }
      configureRecording(allEntitiesPattern, false, false)
      configureWarmUpPhase()
    }
  }

  /** Configures warm-up phase. */
  def configureWarmUpPhase() = {
    if (warmUpPhase.isDefined) {
      require(warmUpPhase.get.asSecondsOrUnitless <= observationTimes.head,
        "Warm-up phase " + warmUpPhase.get + " overlaps with smallest observation time, " + observationTimes.head + ".")
      write("warmup-period", if (warmUpPhase.get.time > 0) warmUpPhase.get.time.toString else warmUpPhase.get.toSeconds + "s")
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
  }

  /** Sets the warm-up phase length. */
  def warmup_=(duration: Duration) = {
    require(!warmUpPhase.isDefined, "Attempt to define warm-up phase *twice*, once as " + warmUpPhase.get + " and once as " + duration + ".")
    warmUpPhase = Some(duration)
  }

  /** Get warm-up phase duration when set, otherwise a duration of zero. */
  def warmup = warmUpPhase.getOrElse(Duration())

  abstract override def considerResults(runId: Int, workingDir: File) = {
    super.considerResults(runId, workingDir)

    //If a scalar or vector is recorded but not found in the bindings, a wildcard pattern was used and
    // the matched name should be added
    lazy val availableInternalNames = variableBindings.keys.toSet
    def ensureAvailability(name: String) = if (!availableInternalNames(name)) observe(name)

    val directory = workingDir.toString

    //Handle vector data
    if (ResultReader.isVectorDataAvailable(directory, runId) && !observationTimes.isEmpty)
      ResultReader.readVectorFile(directory, runId).values.foreach {
        vectorData =>
          {
            ensureAvailability(vectorData._1.name)
            addVectorValuesFor(runId, vectorData._1.name, vectorData)
          }
      }

    //Handle scalar data
    if (ResultReader.isScalarDataAvailable(directory, runId))
      ResultReader.readScalarFile(workingDir.toString(), runId).foreach {
        scalarData =>
          {
            ensureAvailability(scalarData._1)
            addValueFor(runId, scalarData._1, (0, scalarData._2))
          }
      }
  }

  /** Retrieves all values that need to be retained in memory for latter analysis. */
  def addVectorValuesFor(runId: Int, internalName: String, vectorData: (VectorEntry, List[VectorDataEntry])) {
    require(!observationTimes.isEmpty, "No times defined for which " + internalName + " shall be observed.")
    val vector = vectorData._1
    val interpolationPoints = Interpolation.findInterpolationPoints(vectorData._2.map(_.time(vector)), observationTimes)
    for (interpolationPoint <- interpolationPoints) {
      addValueFor(runId, vector.name, (interpolationPoint._2, vectorData._2(interpolationPoint._1._1).value(vector)))
    }
  }

}
