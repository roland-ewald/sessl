package sessl.omnetpp

import scala.util.parsing.combinator._
import java.io.FileReader
import java.io.BufferedReader
import java.io.File

/**
 * Read results from the output files.
 *
 * @author Roland Ewald
 *
 */
object ResultReader {

  /** Reads vector file for a given run. */
  def readVectorFile(workingDirectory: String, runId: Int) = {
    //    val vectorFile = new File(workingDirectory + "results/General-" + runId + ".vec")
    //    require(vectorFile.canRead, "Cannot read file " + vectorFile.getAbsolutePath)
    //    val reader = new BufferedReader(new FileReader(vectorFile))
    //    var currentLine = reader.readLine
    //    while (currentLine != null) {
    //      println(currentLine)
    //      currentLine = reader.readLine
    //    }
  }

  def readScalarFile(runId: Int) = {

  }

}