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