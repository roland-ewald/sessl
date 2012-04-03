package sessl.tools
import java.io.FileWriter
import scala.collection.mutable.ListBuffer

/** Simple utility to store CSV files.
 *  @author Roland Ewald
 */
class CSVFileWriter(fileName: String) {

  /** The file writer. */
  val writer = new FileWriter(fileName, true)

  /** Stores elements in CSV file. */
  def store(elements: Any*) = {
    val lb = ListBuffer[Any]()
    for (element <- elements) element match {
      case seq: Seq[_] => lb ++= seq
      case x => lb += x
    }
    writer.append(lb.toList.mkString(",") + "\n")
    writer.flush()
  }

  /** Support C++ I/O notation, might be more intuitive for some. */
  def <<(elements: Any*) = store(elements)

}

/** Factory method for file writer. */
object CSVFileWriter {
  def apply(fileName: String = "untitled.csv") = new CSVFileWriter(fileName)
}