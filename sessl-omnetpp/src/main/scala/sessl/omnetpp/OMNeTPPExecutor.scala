package sessl.omnetpp

import java.io.File
import scala.sys.process._

/**
 * Executor for OMNeT++ runs.
 *
 * @author Roland Ewald
 *
 */
object OMNeTPPExecutor {

  /** Executes a single OMNeT++ simulation run. */
  def execute(workingDir: File, executable: File, runNumber: Int) = {
    val cmd = "cmd /c " + executable.getName + " -u Cmdenv -r " + runNumber
    println("Executing '" + cmd + "' in " + workingDir.getAbsolutePath) //TODO logging
    Process(cmd, workingDir)!
  }

}