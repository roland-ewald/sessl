package sessl.omnetpp

import sessl.util.ResultHandling
import java.io.File

/**
 * Interface of result handlers for OMNeT++.
 *
 * @author Roland Ewald
 *
 */
trait OMNeTPPResultHandler extends ResultHandling {

  /**
   * Override this method to process the results for the given run.
   */
  protected[omnetpp] def considerResults(runId: Int, workingDir: File) = { resultsHandlingIsCalled() }

}