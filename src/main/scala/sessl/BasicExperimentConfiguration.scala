package sessl

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import sessl.util.MiscUtils

/** Trait realizing the basic data-management and event-handling functionality of experiments.
 *  Event handles can be added for execution after every run, after every set of replications, or
 *  after the whole experiment.
 *
 *  The results have to be handed over to each event handler, so they are managed here as well. The underlying
 *  simulation system needs to signal when a run or a set of replications is done (via runDone(...)/replicationsDone(...)),
 *  and it has to define a (variable) assignment for each run, so that it can be associated with the correct set of replications.
 *
 *  Also, it is possible to augment the results of run, replication set, and experiment with additional data, so-called 'aspects',
 *  which can be added via the interface of this trait as well.
 *
 *  Finally, the trait checks whether a stacked mix-in composition was successful, by checking whether its own runDone(...) etc. methods
 *  have been called - as it is presumably at the bottom of this hierarchy. This should help to detect erroneous behavior of certain traits
 *  (more specifically: not calling the super method although overriding one of the methods that demand this).
 *
 *
 *  @see sessl.ExperimentResults
 *  @see sessl.Experiment
 *  @see sessl.RunResultsAspect
 *  @see sessl.ReplicationsResultsAspect
 *  @see sessl.ExperimentResultsAspect
 *
 *  @author Roland Ewald
 */
private[sessl] trait BasicExperimentConfiguration extends ExperimentConfiguration {

  /** Assignment storage, maps assignment ID => variable assignment. */
  private[this] val assignments = Map[Int, VariableAssignment]()

  /** Maps run IDs => assignment IDs. */
  private[this] val runAssignments = Map[Int, Int]()

  /** The actions to be done after a run has been finished. */
  private[this] val runDoneActions = ListBuffer[RunResults => Unit]()

  /** The actions to be done after the replications for a certain setup have been finished. */
  private[this] val replicationsDoneActions = ListBuffer[ReplicationsResults => Unit]()

  /** The actions to be done after the experiment has been finished. */
  private[this] val experimentDoneActions = ListBuffer[ExperimentResults => Unit]()

  /** A general store for the experiment results.*/
  private[this] val experimentResults = new ExperimentResults()

  /** The flag to check whether runDone() has been properly called. */
  private[this] var runDoneCalled = false

  /** The flag to check whether replicationsDone() has been properly called. */
  private[this] var replicationsDoneCalled = false

  /** The flag to check whether experimentDone() has been properly called. */
  private[this] var experimentDoneCalled = false

  /** Execute a given function after a run has ended.
   *  @param f the function
   */
  def afterRun(f: RunResults => Unit) = { runDoneActions += f }

  /** Execute a given function after the replications for a setup have ended.
   *  @param f the function to be executed when the replications for a setup are done
   */
  def afterReplications(f: ReplicationsResults => Unit) = { replicationsDoneActions += f }

  /** Execute a given function after the experiment has ended.
   *  @param f the function to be executed when the experiment is done
   */
  def afterExperiment(f: ExperimentResults => Unit) = { experimentDoneActions += f }

  /** Adds a results aspect for a certain run.
   *  @param runId the id of the given run
   *  @param a the result aspect
   */
  def addRunResultsAspect(runId: Int, a: RunResultsAspect) = { experimentResults.addAspectForRun(runId, a) }

  /** Adds a replications result aspect to a certain variable assignment.
   *  @param assignmentId the assignment id
   *  @param a the aspect
   */
  def addReplicationsResultsAspect(assignmentId: Int, a: ReplicationsResultsAspect) = { experimentResults.addAspectForReplications(assignmentId, a) }

  /** Adds the experiment results aspect.
   *  @param a the aspect
   */
  def addExperimentResultsAspect(a: ExperimentResultsAspect) = { experimentResults.addAspect(a) }

  /** Adds a variable assignment to the internal in-memory 'database'. Needs to be called for every new assignment.
   *  @param runID the id of the simulation run
   *  @param assignID the id of the assignment
   *  @param assignment the assignment
   */
  def addAssignmentForRun(runId: Int, assignId: Int, assignment: VariableAssignment) = {
    if (assignments.contains(assignId))
      Result.checkAssignmentEquality(assignments(assignId), assignment)
    else
      assignments(assignId) = assignment
    runAssignments(runId) = assignId
  }

  /** Checks if experiment is done.*/
  def isDone = experimentDoneCalled

  override final def runDone(runId: Int) = {
    synchronized {
      runDoneCalled = true
      registerFinishedRun(runId)
      collectRunResultsAspects(runId)
      executeRunDoneActions(runId)
    }
  }

  override final def replicationsDone(assignId: Int) = {
    synchronized {
      replicationsDoneCalled = true
      registerFinishedReplications(assignId)
      collectReplicationsResultsAspects(assignId)
      executeReplicationsDoneActions(assignId)
    }
  }

  override final def experimentDone() = {
    synchronized {
      require(runDoneCalled, "For each finished and non-empty experiment, runDone() shold have been called at least once.")
      require(replicationsDoneCalled, "For each finished and non-empty experiment, replicationsDone() shold have been called at least once.")
      experimentDoneCalled = true
      collectExperimentResultsAspects()
      executeExperimentDoneActions()
    }
  }

  /** Registers in data structures that a run has been finished.
   *  @param runId the run id
   */
  private[this] def registerFinishedRun(runId: Int) = {
    require(runAssignments.contains(runId), "No assignment has been recorded for run with id " + runId)
    val variableAssignment = assignments(runAssignments(runId))
    experimentResults += new RunResults(runId, variableAssignment)
  }

  /** Registers in data structures that a set of replications has been finished.
   *  @param assignmentId the assignment id
   *  @return variable assignment shared by the replications that have just been finished
   */
  private[this] def registerFinishedReplications(assignmentId: Int) = {
    experimentResults += new ReplicationsResults(assignmentId)
    val associatedRunIds = runAssignments.filter(assignEntry => assignEntry._2 == assignmentId).keys.toSet
    runAssignments --= associatedRunIds
    val replicationsResults = experimentResults.forAssignment(assignmentId)
    associatedRunIds.foreach(id => replicationsResults += experimentResults.forRun(id))
    assignments.remove(assignmentId).get
  }

  /** Executes all run done actions.
   *  @param runId the run id
   */
  private[this] def executeRunDoneActions(runId: Int) = {
    if (!runDoneActions.isEmpty)
      runDoneActions.foreach(MiscUtils.saveApply(_, experimentResults.forRun(runId)))
  }

  /** Execute all replications done actions.
   *  @param assignmentId the assignment id
   */
  private[this] def executeReplicationsDoneActions(assignmentId: Int) = {
    if (!replicationsDoneActions.isEmpty)
      replicationsDoneActions.foreach(MiscUtils.saveApply(_, experimentResults.forAssignment(assignmentId)))
  }

  /** Executes all experiment done actions. */
  private[this] def executeExperimentDoneActions() = {
    if (!experimentDoneActions.isEmpty)
      experimentDoneActions.foreach(MiscUtils.saveApply(_, experimentResults))
  }

}