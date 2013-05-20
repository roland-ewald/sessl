/**
 * *****************************************************************************
 * Copyright 2013 Roland Ewald
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
package sessl.util

/**
 * Simple helper to re-use the configuration of parallel execution.
 *
 * @author Roland Ewald
 *
 */
object ParallelExecutionConfiguration {

  val processors = Runtime.getRuntime.availableProcessors

  /**
   * Calculates the number of parallel threads to be used. The (default) value '0' means that
   *  for each available processor one thread shall be used, negative numbers like '-x' indicate that x processors
   *  should stay idle, and positive numbers like 'x' indicate that x processors should be busy (i.e., should have
   *  a thread running on them).
   *
   *  @see Runtime#getRuntime()#availableProcsessors()
   *
   *  @param userDefinedThreads thread number as defined by the user
   *  @return tuple (actual number of threads to be used, potential warning message)
   */
  def calculateNumberOfThreads(userDefinedThreads: Int): (Int, Option[String]) = {

    var warningString: Option[String] = None

    val threads: Int = {
      if (userDefinedThreads > 0) userDefinedThreads
      else if (userDefinedThreads == 0) processors
      else if (processors + userDefinedThreads < 1) {
        warningString = Some("Number of parallel threads to be left idle (" + userDefinedThreads.abs + ") <= number of available processors (" + processors + "); continuing execution with just a single thread.")
        1
      } else processors + userDefinedThreads
    }

    (threads, warningString)
  }

}