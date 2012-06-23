package sessl.omnetpp

import scala.util.parsing.combinator._
import java.io.FileReader
import java.io.BufferedReader
import java.io.File

/** Read results from the output files.
 *
 *  @author Roland Ewald
 *
 */
object ResultReader {

  /** The representation of vector data. */
  type VectorData = (VectorEntry, List[VectorDataEntry])

  /** The result file parser. */
  private val rfParser = new ResultFileParser

  /** The path (relative to working directory) where the results can be found. */
  private val resultLocationAndPrefix = "results/General-"

  /** Reads vector file for a given run. */
  def readVectorFile(workingDirectory: String, runId: Int): Map[Int, VectorData] = {
    processFile(retrieveFileName(workingDirectory, runId, "vec"))(processVectorFileResults)
  }

  /** Reads scalar file for a given run. */
  def readScalarFile(workingDirectory: String, runId: Int): Map[String, AnyVal] = {
    processFile(retrieveFileName(workingDirectory, runId, "sca"))(processScalarFileResults)
  }

  /** Parses a file and processes the results. */
  private def processFile[X](fileName: String)(resultProcessing: (String, List[Product]) => X): X = {
    val result = rfParser.parse(fileName)
    require(result.successful, "An error occurred while parsing file '" + fileName + "'.")
    resultProcessing(fileName, result.get.filter(_.isInstanceOf[ResultElement]))
  }

  /** Creates the name of the file to be read. */
  private def retrieveFileName(workingDirectory: String, runId: Int, ending: String) =
    workingDirectory + resultLocationAndPrefix + runId + '.' + ending

  /** Process results from a vector data file. */
  private def processVectorFileResults(fileName: String, results: List[Product]) = {
    val rv = scala.collection.mutable.Map[Int, VectorData]()
    results.foreach {
      _ match {
        case ver: VersionEntry => checkVersion(fileName, ver)
        case vec: VectorEntry => {
          require(!rv.contains(vec.id), "Vector with ID " + vec.id + " is defined *twice* in file '" + fileName + "'.")
          rv(vec.id) = (vec, List())
        }
        case data: VectorDataEntry => {
          require(rv.contains(data.id), "Vector with ID '" + data.id + "' has not been defined yet.")
          val currentData = rv(data.id)
          rv(data.id) = (currentData._1, data :: currentData._2)
        }
        case x => throw new IllegalArgumentException("Unsupported element in file '" + fileName + "': " + x)
      }
    }
    //Revert lists of vector data to correct temporal order, add to immutable map
    Map() ++ rv.map(x => (x._1, (x._2._1, x._2._2.reverse)))
  }

  /** Process results from a scalar data file. */
  private def processScalarFileResults(fileName: String, results: List[Product]) = {
    val rv = scala.collection.mutable.Map[String, AnyVal]()
    results.foreach {
      case ver: VersionEntry => checkVersion(fileName, ver)
      case sca: ScalarDataEntry => {
        require(!rv.contains(sca.name), "Scalar with name '" + sca.name + "' already set!")
        if (sca.value.isInstanceOf[Int]) {
          //TODO 
        } else println("Warning: could not include non-numeric value '" + sca.value + "' for scalar '" + sca.name + "'")
        //TODO: logging
      }
      case x => throw new IllegalArgumentException("Unsupported element in file '" + fileName + "': " + x)
    }
    Map() ++ rv
  }

  /** Checks if the correct version is declared. */
  private def checkVersion(fileName: String, v: VersionEntry) = {
    require(v.isSupportedVersion,
      "Version '" + v.version + "' of this format is currently not supported, use version " + v.supportedVersion + " in file '" + fileName + "' instead.")
  }

}