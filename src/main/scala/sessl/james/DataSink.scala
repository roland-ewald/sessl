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
  this: ExperimentOn with Instrumentation =>

  abstract override def configure() {
    super.configure()
    if (dataSinkSpecification.isDefined)
      dataSinkSpecification.get match {
        case ds: MySQLDataSink => exp.setDataStorageFactory(new ParameterizedFactory[DataStorageFactory](new JdbcDataStorageFactory,
          Param() :/ (PARAM_URL ~> ("jdbc:mysql://" + ds.host + "/" + ds.schema), PARAM_USER ~> ds.user, PARAM_PASSWORD ~> ds.password, PARAM_DRIVER ~> "com.mysql.jdbc.Driver")))
        case ds: DatabaseDataSink => exp.setDataStorageFactory(new ParameterizedFactory[DataStorageFactory](new JdbcDataStorageFactory,
          Param() :/ (PARAM_URL ~> ds.url, PARAM_USER ~> ds.user, PARAM_PASSWORD ~> ds.password, PARAM_DRIVER ~> ds.driver)))
        case ds => throw new IllegalArgumentException("Data sink of type '" + ds + "' is not supported.")
      }
  }

}

/** Convenience class to avoid having to look up the correct driver/URI scheme*/
case class MySQLDataSink(host: String = "localhost", schema: String = "schema", user: String = "root", password: String = "root") extends DataSinkSpecification