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

import scala.collection.mutable.ListBuffer

import sessl.util.MiscUtils._

/**
 * General support for result reports.
 * It lets the user construct a hierarchy of [[ReportNode]] elements to structure the report in sections etc.,
 * which are then displayed in the defined order.
 * Each (sub-)section may have an arbitrary number of [[DataView]] entities, which represent plots or tables.
 *
 * A report has a name, a description, and a target directory that can be assigned directly.
 * Report sections and data views are specified by function calls.
 *
 * @example {{{
 * new Experiment with Observation with Report {
 *   // ...
 *   reportName = "The name of the report"
 *   reportDescription = "The description"
 *   reportDirectory = "../target_directory"
 *   // ...
 *   withRunResult { results =>
 *     reportSection("New Section") {
 *      reportSection("New Subsection") {
 *        linePlot(results.all)(title = "The results of the run")
 *      }
 *     }
 *    }
 * }
 * }}}
 *
 *  @author Roland Ewald
 */
trait AbstractReport extends ExperimentConfiguration {
  this: AbstractExperiment =>

  /** The name of the report. */
  var reportName = "Untitled Report"

  /** The description of the report. */
  var reportDescription = ""

  /** The directory where the report shall be stored. */
  var reportDirectory = "."

  /** The topmost report section. */
  private val rootSection = RootReportSection

  /** The section to be currently filled with content. */
  private var currentSection: ReportSection = rootSection

  /**
   * Defines a new report section.
   *  @param name the name of the section
   *  @param description the description of the section (default: empty)
   *  @param contentSpecification the function defining the content of the section
   */
  def reportSection(name: String, description: String = "")(contenSpecification: => Unit) {
    currentSection = ReportSectionNode(name, description, currentSection)
    contenSpecification
    currentSection = currentSection.parent
  }

  /**
   * Defines a scatter plot.
   *  @param xData data for the x-axis
   *  @param yData data for the y-axis
   *  @param title title of the plot (default: empty)
   *  @param caption the caption (default: empty)
   *  @param xLabel label for the x-axis (default: empty)
   *  @param yLabel label for the y-axis (default: empty)
   */
  def scatterPlot(xData: Any, yData: Any)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val namedXData = toNamedList(xData)(0)
    val namedYData = toNamedList(yData)(0)
    if (dataAreNumeric(namedXData._2, namedYData._2)) {
      currentSection.childs += ScatterPlotView(toDoubleList(namedXData._2), toDoubleList(namedYData._2), title, caption,
        getOrEmpty(xLabel, namedXData._1), getOrEmpty(yLabel, namedYData._1), currentSection)
    }
  }

  /**
   * Define a histogram plot.
   *  @param data the data to be plotted
   *  @param title title of the plot (default: empty)
   *  @param caption the caption (default: empty)
   *  @param xLabel label for the x-axis (default: empty)
   *  @param yLabel label for the y-axis (default: 'Amount')
   */
  def histogram(data: Any)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "Amount") = {
    val namedData = toNamedList(data)(0)
    if (dataIsNumeric(namedData._2))
      currentSection.childs += HistogramView(toDoubleList(namedData._2), title, caption, getOrEmpty(xLabel, namedData._1), yLabel, currentSection)
  }

  /**
   * Define a box plot with variable names.
   *  @param data list of tuples of the form '(name, list of values)'
   *  @param title title of the plot (default: empty)
   *  @param caption the caption (default: empty)
   *  @param xLabel label for the x-axis (default: empty)
   *  @param yLabel label for the y-axis (default: empty)
   */
  def boxPlot(data: Any*)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val namedData = data.flatMap(toNamedList)
    if (namedData.forall(x => dataIsNumeric(x._2)))
      currentSection.childs += BoxPlotView(namedData.toList.map(x => (x._1, toDoubleList(x._2))), title, caption, xLabel, yLabel, currentSection)
  }

  /**
   * Define a line plot with variable names. Time points are taken from the first trajectory in the list.
   *  @param data list of [[Trajectory]] or (variable name, [[Trajectory]]) tuples
   *  @param title title of the plot (default: empty)
   *  @param caption the caption (default: empty)
   *  @param xLabel label for the x-axis (default: empty)
   *  @param yLabel label for the y-axis (default: empty)
   */
  def linePlot(data: Any*)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val convertedData = convertToLinePlotData(retrieveInstrumentationResult(data))
    if (convertedData.forall(x => dataIsNumeric(x._2)))
      currentSection.childs += LinePlotView(convertedData.map(x => (x._1, toDoubleList(x._2))), title, caption, xLabel, yLabel, currentSection)
  }

  /** Checks whether the whole result of a simulation run has been passed, in which case it is retrieved. */
  private[this] def retrieveInstrumentationResult(data: Iterable[Any]) = data.head match {
    case result: ObservationRunResultsAspect =>
      require(data.size == 1, "Only single-element result list is allowed."); result.all
    case _ => data
  }

  /** Converts data items to a common format that can be used for line plots. */
  private[this] def convertToLinePlotData(data: Iterable[Any]): Seq[(String, Iterable[_])] = data.head match {
    case tuple: (_, _) => ("Time", tuple._2.asInstanceOf[Trajectory].map(_._1)) :: data.toList.flatMap(toNamedList)
    case multipleData: Iterable[_] => multipleData.head match {
      case tuple: (_, _) =>
        tuple._1 match {
          case s: String => {
            require(data.size == 1, "Expecting just another nesting layer if data does not consist of (name, trajectory) tuples.")
            convertToLinePlotData(multipleData)
          }
          case _ => throw new IllegalArgumentException
        }
      case _ => throw new IllegalArgumentException
    }
    case _ => data.toList.flatMap(toNamedList)
  }

  /**
   * Define a report section that describes the outcomes of a statistical test.
   *  @param firstData data of the first sample
   *  @param secondData data of the second sample
   *  @param caption the caption of the output (default: empty)
   *  @param test the [[TwoPairedStatisticalTest]] to be used
   */
  def reportStatisticalTest(firstData: Any, secondData: Any)(caption: String = "", test: TwoPairedStatisticalTest = null) = {
    val namedFirstData = toNamedList(firstData)(0)
    val namedSecondData = toNamedList(secondData)(0)
    if (dataAreNumeric(namedFirstData._2, namedSecondData._2))
      currentSection.childs += StatisticalTestView((namedFirstData._1, toDoubleList(namedFirstData._2)),
        (namedSecondData._1, toDoubleList(namedSecondData._2)), caption, if (test == null) None else Some(test), currentSection)
  }

  /**
   * Define a report section that prints out data in a table format.
   *  @param data a list of [[Trajectory]] or tuples of the form (variable name, [[Trajectory]])
   *  @param caption the caption of the output (default: empty)
   */
  def reportTable(data: Any*)(caption: String = "") = {
    val namedData = data.flatMap(toNamedList)
    val tableData = namedData.map(x => x._1 :: x._2.map(_.toString).toList)
    currentSection.childs += TableView(tableData, caption, currentSection)
  }

  /** Converts an object of a suitable type into a tuple (name, list of values)*/
  private[this] def toNamedList(data: Any): Seq[(String, Iterable[_])] = {
    data match {
      //Because of type erasure, we have to match the types of the list content separately
      case tuple: (_, _) => tuple._2.asInstanceOf[Iterable[_]].head match {
        case t: (_, _) => Seq((tuple._1.toString, tuple._2.asInstanceOf[Trajectory].map(_._2)))
        case _ => Seq((tuple._1.toString, tuple._2.asInstanceOf[Iterable[_]]))
      }
      case seq: Seq[_] => seq.head match {
        case t: (_, _) => seq.asInstanceOf[Seq[(_, Iterable[Double])]].map(tuple => (tuple._1.toString, tuple._2))
        case nestedSeq: Seq[_] => seq.asInstanceOf[Seq[Iterable[_]]].map(rawData => ("", rawData))
        case n: Number => Seq(("", seq.asInstanceOf[Iterable[Number]].map(_.doubleValue()).toList))
      }
      case _ => throw new IllegalArgumentException("Entity '" + data + "' cannot be converted to a tuple (name, list of values).")
    }
  }

  override def configure() {
    super.configure()
    afterExperiment(generateReport)
  }

  /** Get topmost report elements. */
  protected def topmostElements = rootSection.children

  /** Checks whether the elements in the list are numeric. */
  private def dataIsNumeric(values: Iterable[_]): Boolean = {
    val typesOK = typesConform(classOf[Number], values)
    if (!typesOK)
      logger.warn("Some elements to be plotted are not numbers, hence this plot view is skipped: " + values)
    typesOK
  }

  /** Checks whether the elements in all given lists are numeric. */
  private def dataAreNumeric(data: Iterable[_]*): Boolean = data.forall(dataIsNumeric)

  /** Converts a list of Any into a list of Double.*/
  private def toDoubleList(values: Iterable[_]) = values.asInstanceOf[Iterable[Number]].map(_.doubleValue()).toSeq

  /** Needs to be realized by concrete implementations*/
  def generateReport(results: ExperimentResults): Unit
}

//Type hierarchy to represent the report

/** Super trait for all report elements. */
trait ReportNode {
  def parent: ReportSection
}

//Sections

/**Report sections can have child elements. */
trait ReportSection extends ReportNode {
  protected[sessl] val childs = ListBuffer[ReportNode]()
  def children = childs.toList
}

/** The root section has no parent. */
private case object RootReportSection extends ReportSection { def parent = throw new UnsupportedOperationException("The root has no parent.") }

/** Normal nodes representing report sections. */
protected case class ReportSectionNode(name: String, description: String, parent: ReportSection) extends ReportSection {
  parent.childs += this
}

//Data views

/** The super type of all data views. */
trait DataView extends ReportNode

/** Display a scatter plot. */
case class ScatterPlotView(xData: Iterable[Double], yData: Iterable[Double], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a histogram. */
case class HistogramView(data: Iterable[Double], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a box plot. */
case class BoxPlotView(data: Iterable[(String, Iterable[Double])], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a line plot. */
case class LinePlotView(data: Iterable[(String, Iterable[Double])], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display the results of a statistical test. */
case class StatisticalTestView(firstData: (String, Iterable[Double]), secondData: (String, Iterable[Double]), caption: String, testMethod: Option[TwoPairedStatisticalTest], parent: ReportSection) extends DataView

/** Display the results in a table. */
case class TableView(data: Seq[Seq[String]], caption: String, parent: ReportSection) extends DataView
