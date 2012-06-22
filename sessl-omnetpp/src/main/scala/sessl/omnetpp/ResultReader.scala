package sessl.omnetpp

import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import scala.util.parsing.combinator._

/**
 * Read results from the output files.
 *
 * @author Roland Ewald
 *
 */
object ResultReader {

  /** Reads vector file for a given run. */
  def readVectorFile(workingDirectory: String, runId: Int) = {
    val vectorFile = new File(workingDirectory + "results/General-" + runId + ".vec")
    require(vectorFile.canRead, "Cannot read file " + vectorFile.getAbsolutePath)
    val reader = new BufferedReader(new FileReader(vectorFile))
    var currentLine = reader.readLine
    while (currentLine != null) {
      println(currentLine)
      currentLine = reader.readLine
    }
  }

  def readScalarFile(runId: Int) = {

  }

}

/**
 * See Appendix (ch. 25) in http://omnetpp.org/doc/omnetpp/manual/usman.html.
 */
class OMNeTPPResultFileParser extends JavaTokenParsers {
  def value = stringLiteral | floatingPointNumber | wholeNumber
  def versionEntry = "version" ~ wholeNumber ~ "\n"
  def runEntry = "run" ~ wholeNumber ~ "\n"
  def vectorEntry = wholeNumber //...
  def entryType = "run" | "attr" | "param" | "scalar" | "vector" | "file" | "statistic" | "field" | "bin"
}