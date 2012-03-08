package sessl.james

import james.core.experiments.taskrunner.plugintype.TaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory
import james.core.experiments.taskrunner.parallel.ParallelComputationTaskRunnerFactory.NUM_CORES
import james.core.parameters.ParameterizedFactory
import simspex.adaptiverunner.AdaptiveComputationTaskRunner
import simspex.adaptiverunner.AdaptiveTaskRunnerFactory
import simspex.adaptiverunner.policies.EpsilonGreedyDecrInitFactory
import james.SimSystem
import java.util.logging.Level

import sessl._

/** Support for configuring the parallel execution in James II.
 *
 *  @author Roland Ewald
 *
 */
trait ParallelExecution extends AbstractParallelExecution {
  this: Experiment =>

  override def configureParallelExecution(numThreads: Int) = {
    val parameters = Param() :/ (NUM_CORES ~> numThreads)
    if (simulatorSet.size > 1) {
      SimSystem.report(Level.INFO, "Adapting the configuration of the adaptive task runner to use " + numThreads + " threads.");
      val trFactory = exp.getTaskRunnerFactory()
      require(trFactory.getFactory().getClass().isAssignableFrom(classOf[AdaptiveTaskRunnerFactory]))
      trFactory.setParameter(trFactory.getParameters() :/ (NUM_CORES ~> numThreads))
    } else {
      exp.setTaskRunnerFactory(new ParameterizedFactory[TaskRunnerFactory](new ParallelComputationTaskRunnerFactory, parameters))
    }
  }
}