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

