package sessl.james.tools

import java.io.FileWriter

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import james.core.util.misc.Strings

/** Simple utility to store CSV files.
 *
 *  @param fileName the name of the file to be written
 *  @param append whether to append to an existing file with that name or not
 *
 *  @see java.io.FileWriter
 *
 *  @author Roland Ewald
 */
class CSVFileWriter(fileName: String, val append: Boolean = true) {

  /** The file writer. */
  val writer = new FileWriter(fileName, append)

  /** Stores elements in CSV file. */
  def store(elements: Any*) = {
    val lb = ListBuffer[String]()
    for (element <- elements) element match {
      case seq: Seq[_] => lb += elementsToString(seq)
      case x => lb += "\"" + x.toString + "\""
    }
    writer.append(elementsToString(lb) + "\n")
    writer.flush()
  }

  /** Support C++ I/O notation, might be more intuitive for some. */
  def <<(elements: Any*) = store(elements: _*)

  /** Returns a comma-separated string with all elements in a sequence. */
  def elementsToString(elements: Seq[_]): String = {
    val stringRepresentation = Strings.dispCollection(elements)
    stringRepresentation.slice(1, stringRepresentation.size - 1)
  }

}

/** Factory method for file writer. */
object CSVFileWriter {
  def apply(fileName: String = "untitled.csv") = new CSVFileWriter(fileName)
}