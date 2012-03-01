package sessl.james

import james.core.experiments.taskrunner.plugintype.TaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory.NUM_CORES
import james.core.parameters.ParameterizedFactory
import sessl._
import simspex.adaptiverunner.AdaptiveComputationTaskRunner
import simspex.adaptiverunner.AdaptiveTaskRunnerFactory
import sessl.util.AlgorithmSet

/**
 * Support for configuring the parallel execution in James II.
 *
 * @author Roland Ewald
 *
 */
trait ParallelExecution extends AbstractParallelExecution {
  this: ExperimentOn =>

  /** Flag to signal the usage of the adaptive task runner. */
  private[this] var useAdaptiveRunner = false

  override def configureParallelExecution(threads: Int) = {
    val parameters = Param() :/ (NUM_CORES ~> threads)
    if (useAdaptiveRunner) {
      configureAdaptiveRunner(parameters)
    } else {
      exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new ParallelComputationTaskRunnerFactory, parameters))
    }
  }

  /** Use adaptive task runner in case any simulator could be used. */
  override def configureMultiSimulatorExperiment() = simulatorExecutionMode match {
    case AllSimulators => defineMultiAlgoExperimentAllSimulators()
    case AnySimulator => useAdaptiveRunner = true
  }

  /** Configure experiment to use the adaptive task runner. */
  private[this] def configureAdaptiveRunner(parameters: Param) = {
    val parameterBlocks = ParamBlockGenerator.createParamBlockSet(simulatorSet.asInstanceOf[AlgorithmSet[JamesIIAlgo[Factory]]]).toList
    val paramBlockList = new java.util.ArrayList[ParamBlock]()
    parameterBlocks.foreach(p => paramBlockList.add(p))
    exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new AdaptiveTaskRunnerFactory,
      parameters :/ (AdaptiveTaskRunnerFactory.PORTFOLIO) ~> paramBlockList))
  }

  /** Configure experiment to use the parallel runner. */
  private[this] def configureParallelRunner(parameters: Param) =
    exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new ParallelComputationTaskRunnerFactory, parameters))

}