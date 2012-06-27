package sessl

import sessl.util.Logging

/**
 * Super trait for all types that help to configure an experiment.
 *  If a new general aspect of simulation experiments shall be defined,
 *  it should be implemented as an Abstract*-sub-trait of this trait.
 *  @author Roland Ewald
 */
trait ExperimentConfiguration extends Logging {

  /** The experiment can be configured. As stacked mixins shall be implemented, don't forget to call super.configure()! */
  protected def configure(): Unit = {}

  /**
   * This method *has* to be called by the simulation system after a run has been finished.
   *  Each configuration may execute some code after a single simulation run is done. As stacked mixins shall be implemented, don't forget to call super.runDone()!
   */
  protected def runDone(runID: Int): Unit = {}

  /**
   * This method *has* to be called by the simulation system after a set of replications has been finished.
   *  The configuration may execute some code after replications for a setup are done. As stacked mixins shall be implemented, don't forget to call super.replicationsDone()!
   */
  protected def replicationsDone(assignID: Int): Unit = {}

  /**
   * This method *has* to be called by the simulation system after an experiment has been finished.
   *  The configuration may execute some code after the experiment is done. As stacked mixins shall be implemented, don't forget to call super.experimentDone()!
   */
  protected def experimentDone(): Unit = {}

  /** Override this to add custom run results aspects. As stacked mixins shall be implemented, don't forget to call super.collectRunResultsAspects()!*/
  protected def collectRunResultsAspects(runId: Int) = {}

  /** Override this to add custom replications results aspects. As stacked mixins shall be implemented, don't forget to call collectReplicationsResultsAspects()!*/
  protected def collectReplicationsResultsAspects(assignId: Int) = {}

  /** Override this to add custom experiment results aspects.  As stacked mixins shall be implemented, don't forget to call collectExperimentResultsAspects()!*/
  protected def collectExperimentResultsAspects() = {}

}