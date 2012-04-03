package sessl.tools
import java.io.FileWriter

/** Simple utility to store CSV files.
 */
class CSVFileWriter(fileName: String) {

  val writer = new FileWriter(fileName, true)

  def store(elements: Any*) = {

  }

  def <<(elements: Any*) = store(elements)

}

object CSVFileWriter {
  def apply(fileName: String = "untitled.csv") = new CSVFileWriter(fileName)
}