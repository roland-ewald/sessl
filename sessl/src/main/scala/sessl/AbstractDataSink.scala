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
package sessl

/**
 * Some type definitions for data sinks.
 *
 *  @example {{{
 *  new Experiment with Observation with DataSink {
 *    //...
 *    dataSink = SomeDataSink(...)
 *    //...
 *    }
 *  }}}
 *
 *  @author Roland Ewald
 */
trait AbstractDataSink extends ExperimentConfiguration {
  this: AbstractObservation =>

  /** The data sink to be used. */
  protected[sessl] var dataSinkSpecification: Option[DataSinkSpecification] = None

  /** Set the data sink. */
  def dataSink_=(ds: DataSinkSpecification) = { dataSinkSpecification = Some(ds) }

  /** Get the data sink.*/
  def dataSink: DataSinkSpecification = { dataSinkSpecification.get }
}

/** The super class of all data sink specifications.*/
trait DataSinkSpecification

/**
 * Data sink specification for (JDBC-compatible) databases.
 *
 *  @example {{{
 *  dataSink = DatabaseDataSink(url="jdbc:mysql://localhost/my_schema", username="john_doe", driver="com.mysql.jdbc.Driver")
 * 	}}}
 *
 *  @param url the database URL
 *  @param user the user name
 *  @param password the password
 *  @param driver the class name of the JDBC driver
 */
case class DatabaseDataSink(url: String = "not://specified", user: String = "username", password: String = "", driver: String = "unknown driver")
  extends DataSinkSpecification

/**
 * File-based data sink specification.
 * @example {{{
 * dataSink = FileDataSink("output.dat")
 * }}}
 *  @param file the file name
 */
case class FileDataSink(file: String = "./unspecified.dat") extends DataSinkSpecification

/**
 * In-memory data sink.
 * @example {{{
 * dataSink = MemoryDataSink
 * }}}
 */
case object MemoryDataSink extends DataSinkSpecification

