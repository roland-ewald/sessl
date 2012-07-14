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
package sessl.util

/** This is an auxiliary trait that allows to check whether the result-relying mix-ins have been called correctly.
 *  @author Roland Ewald
 */
trait ResultHandling {

  /** Flag to check whether all mix-ins obeyed the contract and called their super method when overriding the result handler. */
  private[this] var resultsHandlingCalled = false

  /** Controls whether the result handler has been invoked correctly. */
  protected def resultsHandlingIsCalled() = {
    resultsHandlingCalled = true;
  }

  /** Checks whether the results-handling call flag is true. */
  def checkResultHandlingCorrectness(methodName: String) = {
    require(resultsHandlingCalled, "The call chain for '" + methodName + "' has not been invoked correctly, some mix-in violates its contract!")
  }

}
