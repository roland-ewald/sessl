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
package sessl.james

import scala.collection.mutable.Map

import org.jamesii.core.data.DBConnectionData
import org.jamesii.core.experiments.ComputationTaskRuntimeInformation
import org.jamesii.core.experiments.taskrunner.ITaskRunner
import org.jamesii.core.processor.plugintype.ProcessorFactory
import org.jamesii.perfdb.util.HibernateConnectionData
import org.jamesii.perfdb.util.ParameterBlocks
import org.jamesii.simspex.gui.PerfDBRecorder
import org.jamesii.simspex.gui.SimSpExPerspective
import org.jamesii.simspex.util.DBConfiguration.HSQL_DEFAULT_LOCATION_PREFIX
import org.jamesii.simspex.util.DBConfiguration.HSQL_DEFAULT_PWD
import org.jamesii.simspex.util.DBConfiguration.HSQL_DEFAULT_USER
import org.jamesii.simspex.util.DBConfiguration.HSQL_DIALECT
import org.jamesii.simspex.util.DBConfiguration.HSQL_DRIVER
import org.jamesii.simspex.util.DBConfiguration.MYSQL_DIALECT
import org.jamesii.simspex.util.DBConfiguration.MYSQL_DRIVER

import sessl.AbstractPerformanceObservation
import sessl.PerfObsRunResultsAspect
import sessl.PerformanceDataSinkSpecification
import sessl.PerformanceDatabaseDataSink
import sessl.Simulator
import sessl.SupportSimulatorConfiguration

/**
 * Support for performance observation in James II.
 *  @author Roland Ewald
 */
trait PerformanceObservation extends AbstractPerformanceObservation {
  this: Experiment with SupportSimulatorConfiguration =>

  /** The run performances, associated with their run ids. */
  private[this] val runPerformances = Map[Int, PerfObsRunResultsAspect]()

  /** The parameter block string representations, mapped back to their corresponding setups. */
  private[this] val setups = Map[String, Simulator]()

  /** Reference to the database recorder that is used (if any). */
  private[this] var performanceRecorder: Option[PerfDBRecorder] = None

  override def configure() {
    super.configure()
    // Read out all defined algorithm setups
    simulators.algorithms.foreach(algo => {
      val representation = ParameterBlocks.toUniqueString(ParamBlockGenerator.createParamBlock(algo.asInstanceOf[JamesIIAlgo[Factory]]))
      setups(representation) = algo
    })

    require(setups.nonEmpty, "No simulator setups for performance evaluation have been specified.")

    // Fill the run performances map with actual data from the execution listener
    exp.getExecutionController().addExecutionListener(new ExperimentExecutionAdapter() {
      override def simulationExecuted(taskRunner: ITaskRunner,
        crti: ComputationTaskRuntimeInformation, jobDone: Boolean): Unit = {
        //Get string representation for current setup...
        val representation = ParameterBlocks.toUniqueString(
          org.jamesii.core.parameters.ParameterBlocks.getSubBlock(crti.getComputationTask().getConfig().getExecParams(), classOf[ProcessorFactory].getName()))

        //... and look it up in the setups map
        require(setups.contains(representation), "No setup found for parameter block representation: " + representation)
        runPerformances(crti.getComputationTaskID) =
          new PerfObsRunResultsAspect(setups(representation), crti.getRunInformation().getComputationTaskRunTime())
      }
    })
    configurePerformanceDataSink()
  }

  override def collectPerformanceResults(runId: Int, removeData: Boolean): PerfObsRunResultsAspect = {
    val runPerformance = if (removeData)
      runPerformances.remove(runId)
    else
      runPerformances.get(runId)

    runPerformance.getOrElse {
      logger.warn("No performance result for run with id '" + runId +
        "' - returning special results aspect to signal the failure."); new PerfObsRunResultsAspect(null, -1)
    }
  }

  /** Configures, instantiates and starts the performance recorder for the James II performance database. */
  private[this] def configurePerformanceDataSink(): Unit = {
    if (!performanceDataSinkSpecication.isDefined)
      return
    val dbConnectionData = createConnectionData(performanceDataSinkSpecication.get)
    SimSpExPerspective.setDbConnectionData(dbConnectionData)
    performanceRecorder = Some(new PerfDBRecorder())
    exp.getExecutionController().addExecutionListener(performanceRecorder.get)
    performanceRecorder.get.start()
    afterExperiment { _ => performanceRecorder.get.stop() }
  }

  /** Creates performance database connection data. */
  private[this] def createConnectionData(perfDataSinkSpec: PerformanceDataSinkSpecification): DBConnectionData = {
    perfDataSinkSpec match {
      case mysql: MySQLPerformanceDataSink =>
        new HibernateConnectionData("jdbc:mysql://" + mysql.host + '/' + mysql.schema, mysql.user, mysql.password, MYSQL_DRIVER, MYSQL_DIALECT)
      case file: FilePerformanceDataSink =>
        new HibernateConnectionData(HSQL_DEFAULT_LOCATION_PREFIX + file.fileName, HSQL_DEFAULT_USER, HSQL_DEFAULT_PWD, HSQL_DRIVER, HSQL_DIALECT)
      case db: PerformanceDatabaseDataSink => {
        val dialect = db.driver match {
          case MYSQL_DRIVER => MYSQL_DIALECT
          case HSQL_DRIVER => HSQL_DIALECT
          case _ => ""
        }
        require(!dialect.isEmpty(), "Only MySQL and HyperSQL are supported for now, so use '" + MYSQL_DRIVER + "' or '" + HSQL_DRIVER + "' as driver.")
        new HibernateConnectionData(db.url, db.user, db.password, db.driver, dialect)
      }
      case x => throw new IllegalArgumentException("Performance data sink '" + x + "' is not supported.")
    }
  }
}

/** Convenience class to avoid having to look up the correct driver/URI scheme for MySQL.*/
case class MySQLPerformanceDataSink(host: String = "localhost", schema: String = "perf_db", user: String = "root", password: String = "root") extends PerformanceDataSinkSpecification

/** Convenience class to avoid having to look up the correct driver/URI scheme for HSQLDB.*/
case class FilePerformanceDataSink(fileName: String = "perf_db") extends PerformanceDataSinkSpecification
