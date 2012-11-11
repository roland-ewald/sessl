/**
 * *****************************************************************************
 * Copyright 2012 Roland Ewald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package sessl.james.tools

import java.io.FileWriter

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.jamesii.core.util.misc.Strings

/**
 * Simple utility to store CSV files.
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
    val stringRepresentation = Strings.dispIterable(elements)
    stringRepresentation.slice(1, stringRepresentation.size - 1)
  }

}

/** Factory method for file writer. */
object CSVFileWriter {
  def apply(fileName: String = "untitled.csv") = new CSVFileWriter(fileName)
}
