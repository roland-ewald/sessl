package sessl.omnetpp

import java.io.File
import scala.sys.process._
import sessl.util.Logging

/**
 * Executor for OMNeT++ runs.
 *
 * @author Roland Ewald
 *
 */
object OMNeTPPExecutor extends Logging {

  /** Executes a single OMNeT++ simulation run. */
  def execute(workingDir: File, executable: File, runNumber: Int) = {
    val cmd = "cmd /c " + executable.getName + " -u Cmdenv -r " + runNumber
    logger.info("Executing '" + cmd + "' in " + workingDir.getAbsolutePath)
    Process(cmd, workingDir)!
  }

}