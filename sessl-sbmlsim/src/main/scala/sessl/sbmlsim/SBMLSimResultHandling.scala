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

import org.simulator.math.odes.MultiTable
import sessl.util.ResultHandling

/**
 * Trait to let mix-ins handle results.
 *  @author Roland Ewald
 */
trait SBMLSimResultHandling extends ResultHandling {

  /**
   * Method to be overridden to extract data from the results.
   *  They use a lot of memory, so the results of each run will be discarded after calling this method.
   */
  protected[sbmlsim] def considerResults(runId: Int, assignmentId: Int, results: MultiTable) = { resultsHandlingIsCalled() }

}
