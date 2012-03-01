package sessl.util

import scala.collection.mutable.Map

import sessl.InstrumentationReplicationsResultsAspect
import sessl.TimeStampedData
import sessl.InstrumentationRunResultsAspect
import sessl.AbstractInstrumentation
import sessl.Experiment
import sessl.Trajectory

/** Provides a simple implementation that supports the general contract of the instrumentation trait.
 */
trait SimpleInstrumentation extends AbstractInstrumentation {
  this: Experiment =>

  /** A naive specification of an in-memory 'database': just a map from run ID => a map of (internal) variable names to trajectories. */
  private[this] val inMemoryDatabase = Map[Int, Map[String, Trajectory]]()

  /** Adds value to the internal in-memory 'database'.
   *
   *  @param runID the ID of the simulation run
   *  @param internalName the internal (model/sim-specific) name of the variable
   *  @param value the observed value for the variable
   */
  protected[sessl] def addValueFor[T](runID: Int, internalName: String, value: TimeStampedData) = {
    val externalNames = variableBindings.get(internalName).get
    val runResults = inMemoryDatabase.getOrElseUpdate(runID, Map())
    for (externalName <- externalNames)
      runResults += ((externalName, value :: runResults.get(externalName).getOrElse(Nil)))

    println("Results for run " + runID + ":" + runResults)
  }

  /** Collects run results from 'database'. */
  override def collectResults(runId: Int, removeData: Boolean = false): InstrumentationRunResultsAspect = {
    if (!inMemoryDatabase.contains(runId)) {
      println("Warning: no results were observed (have you configured the observation?)... creating empty results.") //TODO: Use logging here
      new InstrumentationRunResultsAspect(Map[String, Trajectory]())
    } else {
      val runData = if (removeData) inMemoryDatabase.remove(runId).get else inMemoryDatabase(runId)
      val correctlyOrderedData = runData.map(entry => (entry._1, entry._2.reverse))
      new InstrumentationRunResultsAspect(correctlyOrderedData)
    }
  }

  /** Collects replications results from 'database'. */
  override def collectReplicationsResults(assignId: Int): InstrumentationReplicationsResultsAspect = {
    new InstrumentationReplicationsResultsAspect()
  }

}