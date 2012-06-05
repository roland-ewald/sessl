package sessl.util

import scala.collection.mutable.Map

import sessl.ObservationReplicationsResultsAspect
import sessl.TimeStampedData
import sessl.ObservationRunResultsAspect
import sessl.AbstractObservation
import sessl.AbstractExperiment
import sessl.Trajectory

/** Provides a simple observation mechanism that supports the general contract of the observation trait.
 */
trait SimpleObservation extends AbstractObservation {
  this: AbstractExperiment =>

  /** A naive specification of an in-memory 'database': just a map from run ID => a map of (internal) variable names to trajectories. */
  private[this] val inMemoryDatabase = Map[Int, Map[String, Trajectory]]()

  /** Adds value to the internal in-memory 'database'.
   *
   *  @param runID the ID of the simulation run
   *  @param internalName the internal (model/sim-specific) name of the variable
   *  @param value the observed value for the variable
   */
  protected[sessl] def addValueFor(runID: Int, internalName: String, value: TimeStampedData) = {
    require(variableBindings.contains(internalName), "Internal variable name '" + internalName + "' is not known.")
    val externalNames = variableBindings.get(internalName).get
    val runResults = inMemoryDatabase.getOrElseUpdate(runID, Map())
    for (externalName <- externalNames)
      runResults += ((externalName, value :: runResults.get(externalName).getOrElse(Nil)))
    //println("Added results for run " + runID + ":" + value) //TODO:Use logging here (log-level:finest!)
  }

  /** Collects run results from 'database'. */
  override def collectResults(runId: Int, removeData: Boolean = false): ObservationRunResultsAspect = {
    if (!inMemoryDatabase.contains(runId)) {
      println("Warning: no results were observed (have you configured the observation?)... creating empty results.") //TODO: Use logging here
      new ObservationRunResultsAspect(Map[String, Trajectory]())
    } else {
      val runData = if (removeData) inMemoryDatabase.remove(runId).get else inMemoryDatabase(runId)
      val correctlyOrderedData = runData.map(entry => (entry._1, entry._2.reverse))
      new ObservationRunResultsAspect(correctlyOrderedData)
    }
  }

  /** Collects replications results from 'database'. */
  override def collectReplicationsResults(assignId: Int): ObservationReplicationsResultsAspect = {
    new ObservationReplicationsResultsAspect()
  }

}