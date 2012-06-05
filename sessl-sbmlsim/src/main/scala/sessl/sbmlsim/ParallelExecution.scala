package sessl.sbmlsim

import sessl.AbstractParallelExecution

/** Support for parallel execution.
 *  @author Roland Ewald
 */
trait ParallelExecution extends AbstractParallelExecution {
  this: Experiment =>

  /** The number of threads to be used. */
  private[this] var numberOfThreads = 1

  override def configureParallelExecution(threads: Int) = {
    numberOfThreads = threads
  }

  override def executeJobs(jobs: List[JobDescription]) = {
    collection.parallel.ForkJoinTasks.defaultForkJoinPool.setParallelism(numberOfThreads)
    jobs.par.map(executeJob).toList
  }

}