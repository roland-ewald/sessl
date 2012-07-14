package sessl.util

import sessl.AbstractObservation
import sessl.VariableAssignment
import sessl.TimeStampedData
import sessl.AbstractExperiment
import sessl.BasicExperimentConfiguration
import sessl.ExperimentConfiguration

/** Simple helper trait to mix into observer components.
 *
 *  @param I the required experiment configuration
 *
 *  @author Roland Ewald
 *
 */
trait ObserverHelper[I <: ExperimentConfiguration] {

  /** The ID of this simulation run, needed to register the observations. */
  private[this] var runID: Option[Int] = None

  /** The ID of the assignment (= 'configuration') to which the run belongs, needed to register the observations. */
  private[this] var assignmentID: Option[Int] = None

  /** The variable assignment that is used here. */
  private[this] var assignment: Option[VariableAssignment] = None

  /** The observation configuration. Is called to register the observations. */
  private[this] var obsConfig: Option[I] = None

  def setRunID(id: Int) = { runID = Some(id) }

  def setAssignmentID(id: Int) = { assignmentID = Some(id) }

  def setAssignment(va: VariableAssignment) = { assignment = Some(va) }

  def setConfig(config: I) = { obsConfig = Some(config) }

  /** Checks whether all required data has been set. */
  def checkRequiredData() = {
    require(obsConfig.isDefined, "Observation configuration must be defined.")
    require(runID.isDefined, "Run ID must be set.")
    require(assignmentID.isDefined, "Assignment ID must be set.")
    require(assignment.isDefined, "Assignment must be set.")
  }

  lazy val sesslObsConfig = obsConfig.get

  lazy val sesslRunID = runID.get

  lazy val variableAssignmentID = assignmentID.get

  lazy val variableAssignment = assignment.get

}

/** The Interface SimpleObserverHelper.
 *
 *  @param <I>
 *          the observation type
 */
trait SimpleObserverHelper[I <: SimpleObservation] extends ObserverHelper[I] {

  def addValueFor[T](internalName: String, value: TimeStampedData) = {
    checkRequiredData()
    sesslObsConfig.addValueFor(sesslRunID, internalName, value)
  }
}

class ExperimentObserver extends ObserverHelper[AbstractExperiment] {
  def registerAssignment() {
    sesslObsConfig.asInstanceOf[AbstractExperiment].addAssignmentForRun(sesslRunID, variableAssignmentID, variableAssignment)
  }
}