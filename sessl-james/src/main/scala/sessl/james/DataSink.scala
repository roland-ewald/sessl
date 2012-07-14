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
package sessl.james

import datastorage.jdbc.JdbcDataStorageFactory._
import datastorage.jdbc.JdbcDataStorageFactory
import james.core.data.storage.plugintype.DataStorageFactory
import james.core.parameters.ParameterizedFactory
import sessl._

/** Support for data sinks.
 *
 *  @author Roland Ewald
 *
 */
trait DataSink extends AbstractDataSink {
  this: Experiment with Observation =>

  abstract override def configure() {
    super.configure()
    if (dataSinkSpecification.isDefined)
      dataSinkSpecification.get match {
        case ds: MySQLDataSink => exp.setDataStorageFactory(new ParameterizedFactory[DataStorageFactory](new JdbcDataStorageFactory,
          Param() :/ (PARAM_URL ~>> ("jdbc:mysql://" + ds.host + "/" + ds.schema), PARAM_USER ~>> ds.user, PARAM_PASSWORD ~>> ds.password, PARAM_DRIVER ~>> "com.mysql.jdbc.Driver")))
        case ds: DatabaseDataSink => exp.setDataStorageFactory(new ParameterizedFactory[DataStorageFactory](new JdbcDataStorageFactory,
          Param() :/ (PARAM_URL ~>> ds.url, PARAM_USER ~>> ds.user, PARAM_PASSWORD ~>> ds.password, PARAM_DRIVER ~>> ds.driver)))
        case ds => throw new IllegalArgumentException("Data sink of type '" + ds + "' is not supported.")
      }
  }

}

/** Convenience class to avoid having to look up the correct URI scheme.*/
case class MySQLDataSink(host: String = "localhost", schema: String = "schema", user: String = "root", password: String = "root") extends DataSinkSpecification
