/*******************************************************************************
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
 ******************************************************************************/
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
