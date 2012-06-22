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

  /** The result file parser. */
  val rfParser = new ResultFileParser

  /** The representation of the data for this vector. */
  type VectorData = (VectorEntry, List[VectorDataEntry])

  /** Reads vector file for a given run. */
  def readVectorFile(workingDirectory: String, runId: Int): Map[Int, VectorData] = {
    val fileName = workingDirectory + "results/General-" + runId + ".vec"
    val result = rfParser.parse(fileName)
    require(result.successful, "An error occurred while parsing file '" + fileName + "' for vector data.")
    Map() ++ processVectorFileResults(fileName, result.get)
  }

  /** */
  def processVectorFileResults(fileName: String, results: List[Product]) = {
    val rv = scala.collection.mutable.Map[Int, VectorData]()
    results.filter(_.isInstanceOf[ResultElement]).foreach {
      x =>
        {
          println(x)
          x match {
            case ver: VersionEntry => require(ver.isSupportedVersion,
              "Version '" + ver.version + "' of this format is currently not supported, use version " + ver.supportedVersion + " in file '" + fileName + "' instead.")
            case vec: VectorEntry => {
              require(!rv.contains(vec.id), "Vector with id " + vec.id + "is defined *twice* in file '" + fileName + "'.")
              rv(vec.id) = (vec, List())
            }
            case data: VectorDataEntry => require(rv.contains(data.id), "Vector with ID '" + data.id + "' has not been defined yet.")
            case x => throw new IllegalArgumentException("Unsupported element: " + x)
          }
        }
    }
    rv
  }

  def readScalarFile(runId: Int) = {

  }

}