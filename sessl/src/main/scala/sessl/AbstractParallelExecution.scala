package sessl

/**
 * Support for parallel execution. How the parallel execution is achieved (e.g. fine/coarse-grained in what aspect
 *  is largely up to the simulation system at hand - and to the user specifying the usage of certain algorithms).
 *
 *  The single variable here, the number of parallel threads that shall be employed, just hints at the available resources
 *  that shall be used.
 *
 *  @author Roland Ewald
 *
 */
trait AbstractParallelExecution extends ExperimentConfiguration {

  /**
   * Defines the number of parallel threads to be used for simulation. The (default) value '0' means that
   *  for each available processor one thread shall be used, negative numbers like '-x' indicate that x processors
   *  should stay idle, and positive numbers like 'x' indicate that x processors should be busy (i.e., should have
   *  a thread running on them).
   *
   *  @see Runtime#getRuntime()#availableProcsessors()
   */
  var parallelThreads = 0

  override def configure() {
    super.configure()

    val processors = Runtime.getRuntime.availableProcessors

    val threads: Int = {
      if (parallelThreads > 0) parallelThreads
      else if (parallelThreads == 0) processors
      else if (processors + parallelThreads < 1) {
        logger.warn("Number of parallel threads to be left idle (" + parallelThreads + ") <= number of available processors (" + processors + "); continuing execution with just a single thread.")
        1
      } else processors + parallelThreads
    }

    configureParallelExecution(threads)
  }

  /** Configure the parallel execution for the given number of threads.*/
  def configureParallelExecution(threads: Int)

}