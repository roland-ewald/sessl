package sessl.util

import sessl.AbstractObservation
import sessl.VariableAssignment
import sessl.TimeStampedData
import sessl.AbstractExperiment
import sessl.BasicExperimentConfiguration
import sessl.ExperimentConfiguration

/**
 * Simple helper trait to mix into observer components.
 *
 * @param I the required experiment configuration 
 *
 * @author Roland Ewald
 *
 */
trait ObserverHelper[I <: ExperimentConfiguration] {

  /** The ID of this simulation run, needed to register the observations. */
  private[this] var runID: Option[Int] = None

  /** The ID of the assignment (= 'configuration') to which the run belongs, needed to register the observations. */
  private[this] var assignmentID: Option[Int] = None

  /** The variable assignment that is used here. */
  private[this] var assignment: Option[VariableAssignment] = None

  /** The instrumentation configuration. Is called to register the observations. */
  private[this] var instrConfig: Option[I] = None

  def setRunID(id: Int) = { runID = Some(id) }

  def setAssignmentID(id: Int) = { assignmentID = Some(id) }

  def setAssignment(va: VariableAssignment) = { assignment = Some(va) }

  def setConfig(config: I) = { instrConfig = Some(config) }

  /** Checks whether all required data has been set. */
  def checkRequiredData() = {
    require(instrConfig.isDefined, "Instrumenter configuration must be defined.")
    require(runID.isDefined, "Run ID must be set.")
    require(assignmentID.isDefined, "Assignment ID must be set.")
    require(assignment.isDefined, "Assignment must be set.")
  }

  lazy val sesslInstrConfig = instrConfig.get

  lazy val sesslRunID = runID.get

  lazy val variableAssignmentID = assignmentID.get

  lazy val variableAssignment = assignment.get

}

/**
 * The Interface SimpleObserverHelper.
 *
 * @param <I>
 *          the instrumentation type
 */
trait SimpleObserverHelper[I <: SimpleInstrumentation] extends ObserverHelper[I] {

  def addValueFor[T](internalName: String, value: TimeStampedData) = {
    checkRequiredData()
    sesslInstrConfig.addValueFor(sesslRunID, internalName, value)
  }
}

class ExperimentObserver extends ObserverHelper[AbstractExperiment] {
  def registerAssignment() {
    sesslInstrConfig.asInstanceOf[AbstractExperiment].addAssignmentForRun(sesslRunID, variableAssignmentID, variableAssignment)
  }  
}