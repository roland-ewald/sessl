/**
 * *****************************************************************************
 * Copyright 2012 Roland Ewald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package sessl

import sessl.util.Logging
import sessl.util.ParallelExecutionConfiguration

/**
 * Support for parallel execution. How the parallel execution is achieved (e.g. fine/coarse-grained in what aspect
 *  is largely up to the simulation system at hand - and to the user specifying the usage of specific algorithms).
 *
 *  The single variable here, the number of parallel threads that shall be employed, just hints at the available resources
 *  that shall be used.
 *
 *  @example {{{
 *   new Experiment with ParallelExecution {
 *      //...
 *      parallelThreads = 5  // Use exactly five threads
 *      parallelThreads = 0  // Use one thread per available core (default)
 *      parallelThreads = -2 // Keep two threads idle
 *   }
 *  }}}
 *
 *  @author Roland Ewald
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

    val threadConfig = ParallelExecutionConfiguration.calculateNumberOfThreads(parallelThreads)
    threadConfig._2.map(logger.warn(_))
    configureParallelExecution(threadConfig._1)
  }

  /** Configure the parallel execution for the given number of threads.*/
  def configureParallelExecution(threads: Int)

}
