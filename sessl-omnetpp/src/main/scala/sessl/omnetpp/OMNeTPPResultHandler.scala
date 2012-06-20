package sessl.omnetpp

import sessl.util.ResultHandling
import java.io.File

/**
 * Interface of result handlers for OMNeT++.
 *
 * @author Roland Ewald
 */
trait OMNeTPPResultHandler extends ResultHandling {

  /**
   * Consider results. Override this method to process the results for the given run.
   *
   * @param runId
   *          the run id
   * @param workingDir
   *          the working dir
   */
  protected[omnetpp] def considerResults(runId: Int, workingDir: File) = { resultsHandlingIsCalled() }

}