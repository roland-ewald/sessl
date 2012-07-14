/*******************************************************************************
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
 ******************************************************************************/
package sessl

/** Some type definitions for data sinks.
 *
 *  @author Roland Ewald
 */
trait AbstractDataSink extends ExperimentConfiguration {
  this: AbstractObservation =>

  /** The data sink to be used. */
  protected[sessl] var dataSinkSpecification: Option[DataSinkSpecification] = None

  /** Getting/setting the data sink. */
  def dataSink_=(ds: DataSinkSpecification) = { dataSinkSpecification = Some(ds) }
  def dataSink: DataSinkSpecification = { dataSinkSpecification.get }
}

/** The super class of all data sink specifications.*/
trait DataSinkSpecification

/** Database sinks. */
case class DatabaseDataSink(url: String = "not://specified", user: String = "username", password: String = "", driver: String = "unknown driver")
  extends DataSinkSpecification

/** File-based data sinks. */
case class AbstractFileDataSink(file: String = "./unspecified.dat") extends DataSinkSpecification

/** In-memory data sinks. */
case object MemoryDataSink extends DataSinkSpecification

