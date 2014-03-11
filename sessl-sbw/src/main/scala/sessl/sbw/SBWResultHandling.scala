package sessl.sbw

import sessl.util.ResultHandling

trait SBWResultHandling extends ResultHandling {

  /**
   * Method to be overridden to extract data from the results.
   * They use a lot of memory, so the results of each run will be discarded after calling this method.
   */
  protected[sbw] def considerResults(runId: Int, assignmentId: Int, 
      species: Array[String], results: Array[Array[Double]]) = { resultsHandlingIsCalled() }
}