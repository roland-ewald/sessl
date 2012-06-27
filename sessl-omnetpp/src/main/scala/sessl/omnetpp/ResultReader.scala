package sessl.omnetpp

import scala.util.parsing.combinator._
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import sessl.util.Logging

/**
 * Read results from the output files.
 *
 *  @author Roland Ewald
 *
 */
object ResultReader extends Logging {

  /** The ending of vector data files. */
  val fileEndingVectorData = "vec"

  /** The ending of scalar data files. */
  val fileEndingScalarData = "sca"

  /** The representation of vector data. */
  type VectorData = (VectorEntry, List[VectorDataEntry])

  /** The result file parser. */
  private val rfParser = new ResultFileParser

  /** The path (relative to working directory) where the results can be found. */
  private val resultLocationAndPrefix = "/results/General-"

  /** Checks whether vector data is available. */
  def isVectorDataAvailable(workingDirectory: String, runId: Int) = (new File(getVectorFileName(workingDirectory, runId))).canRead

  /** Checks whether scalar data is available. */
  def isScalarDataAvailable(workingDirectory: String, runId: Int) = (new File(getScalarFileName(workingDirectory, runId))).canRead

  /** Get name of the vector data file. */
  def getVectorFileName(workingDirectory: String, runId: Int) = retrieveFileName(workingDirectory, runId, fileEndingVectorData)

  /** Get name of the scalar data file. */
  def getScalarFileName(workingDirectory: String, runId: Int) = retrieveFileName(workingDirectory, runId, fileEndingScalarData)

  /** Reads vector file for a given run. */
  def readVectorFile(workingDirectory: String, runId: Int): Map[Long, VectorData] = {
    processFile(getVectorFileName(workingDirectory, runId), processVectorFileResults)
  }

  /** Reads scalar file for a given run. */
  def readScalarFile(workingDirectory: String, runId: Int): Map[String, AnyVal] = {
    processFile(getScalarFileName(workingDirectory, runId), processScalarFileResults)
  }

  /** Parses a file and processes the results. */
  private def processFile[X](fileName: String, resultProcessing: (String, List[Product]) => X): X = {
    val result = rfParser.parse(fileName)
    require(result.successful, "An error occurred while parsing file '" + fileName + "'.")
    resultProcessing(fileName, result.get.filter(_.isInstanceOf[ResultElement]))
  }

  /** Creates the name of the file to be read. */
  private def retrieveFileName(workingDirectory: String, runId: Int, ending: String) =
    workingDirectory + resultLocationAndPrefix + runId + '.' + ending

  /** Process results from a vector data file. */
  private def processVectorFileResults(fileName: String, results: List[Product]) = {
    val rv = scala.collection.mutable.Map[Long, VectorData]()
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
        require(!rv.contains(sca.name), "Scalar with name '" + sca.name + "' is already set!")
        if (sca.value.isInstanceOf[Double]) {
          rv(sca.name) = sca.value.asInstanceOf[Double]
        } else if (sca.value.isInstanceOf[Long]) {
          rv(sca.name) = sca.value.asInstanceOf[Long]
        } else {
          logger.warn("Could not include non-numeric value '" + sca.value + "' for scalar '" + sca.name + "'")
        }
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