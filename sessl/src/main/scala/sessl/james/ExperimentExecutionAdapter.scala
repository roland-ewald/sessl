package sessl.james
import james.core.experiments.IExperimentExecutionListener
import james.core.experiments.BaseExperiment
import james.core.experiments.taskrunner.ITaskRunner
import james.core.experiments.ComputationTaskRuntimeInformation

/**
 * An adapter for experiment execution listeners.
 *
 * @author Roland Ewald
 */
class ExperimentExecutionAdapter extends IExperimentExecutionListener {

  override def experimentExecutionStarted(exp: BaseExperiment): Unit = {}

  override def experimentExecutionStopped(exp: BaseExperiment): Unit = {}

  override def simulationExecuted(taskRunner: ITaskRunner,
    crti: ComputationTaskRuntimeInformation, jobDone: Boolean): Unit = {}

  override def simulationInitialized(taskRunner: ITaskRunner,
    crti: ComputationTaskRuntimeInformation): Unit = {}

}