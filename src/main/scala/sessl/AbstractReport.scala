package sessl

import scala.collection.mutable.ListBuffer

import sessl.util.MiscUtils._

/** General support for result reports. It lets the user construct a hierarchy of section/dataview elements,
 *  which are then displayed in this order.
 *
 *  @author Roland Ewald
 *
 */
trait AbstractReport extends ExperimentConfiguration {
  this: AbstractExperiment with AbstractInstrumentation =>

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

  /** Defines a new report section. */
  def reportSection(name: String, description: String = "")(contenSpecification: => Unit) {
    currentSection = ReportSectionNode(name, description, currentSection)
    contenSpecification
    currentSection = currentSection.parent
  }

  /** Define a scatter plot. */
  def scatterPlot(xData: Any, yData: Any)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val namedXData = toNamedList(xData)
    val namedYData = toNamedList(yData)
    if (dataAreNumeric(namedXData._2, namedYData._2)) {
      currentSection.childs += ScatterPlotView(toDoubleList(namedXData._2), toDoubleList(namedYData._2), title, caption,
        getOrEmpty(xLabel, namedXData._1), getOrEmpty(yLabel, namedYData._1), currentSection)
    }
  }

  /** Define a histogram plot. */
  def histogram(data: Any)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "Amount") = {
    val namedData = toNamedList(data)
    if (dataIsNumeric(namedData._2))
      currentSection.childs += HistogramView(toDoubleList(namedData._2), title, caption, getOrEmpty(xLabel, namedData._1), yLabel, currentSection)
  }

  /** Define a box plot with variable names. */
  def boxPlot(data: Any*)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val namedData = data.map(toNamedList)
    if (namedData.forall(x => dataIsNumeric(x._2)))
      currentSection.childs += BoxPlotView(namedData.toList.map(x => (x._1, toDoubleList(x._2))), title, caption, xLabel, yLabel, currentSection)
  }

  /** Define a line plot with variable names. The first value list defines the time points (if it is a trajectory, both values and times are taken from it). */
  def linePlot(data: Any*)(title: String = "", caption: String = "", xLabel: String = "", yLabel: String = "") = {
    val convertedData = data.head match {
      case tuple: (_, _) => ("Time", tuple._2.asInstanceOf[Trajectory].map(_._1)) :: data.toList.map(toNamedList)
      case trajectory: List[_] => ("Time", trajectory.asInstanceOf[Trajectory].map(_._1)) :: data.toList.map(toNamedList)
      case _ => data.toList.map(toNamedList)
    }
    if (convertedData.forall(x => dataIsNumeric(x._2)))
      currentSection.childs += LinePlotView(convertedData.map(x => (x._1, toDoubleList(x._2))), title, caption, xLabel, yLabel, currentSection)
  }

  /** Define a report section that describes the outcomes of a statistical test. */
  def reportStatisticalTest(firstData: Any, secondData: Any)(caption: String = "", test: TwoPairedStatisticalTest = null) = {
    val namedFirstData = toNamedList(firstData)
    val namedSecondData = toNamedList(secondData)
    if (dataAreNumeric(namedFirstData._2, namedSecondData._2))
      currentSection.childs += StatisticalTestView((namedFirstData._1, toDoubleList(namedFirstData._2)),
        (namedSecondData._1, toDoubleList(namedSecondData._2)), caption, if (test == null) None else Some(test), currentSection)
  }

  /** Define a report section that prints out data in a table format. */
  def reportTable(data: Any*)(caption: String = "") = {
    val namedData = data.map(toNamedList)
    val tableData = namedData.map(x => x._1 :: x._2.map(_.toString))
    currentSection.childs += TableView(tableData.toList, caption, currentSection)
  }

  /** Converts an object of a suitable type into a tuple (name, list of values)*/
  private[this] def toNamedList(data: Any): (String, List[_]) = {
    data match {
      //Because of type erasure, we have to match the types of the list content separately
      case tuple: (_, _) => tuple._2.asInstanceOf[List[_]].head match {
        case t: (_, _) => (tuple._1.toString, tuple._2.asInstanceOf[Trajectory].map(_._2))
        case _ => (tuple._1.toString, tuple._2.asInstanceOf[List[_]])
      }
      case list: List[_] => list.head match {
        case t: (_, _) => ("", list.asInstanceOf[List[(Double, _)]].map(_._2))
        case _ => ("", list)
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
  private def dataIsNumeric(values: List[_]): Boolean = {
    val typesOK = typesConform(classOf[Number], values)
    if (!typesOK)
      println("Warning: some elements to be ploted are not numbers, hence this plot view is skipped:" + values) //TODO: Use logging here
    typesOK
  }

  /** Checks whether the elements in all given lists are numeric. */
  private def dataAreNumeric(data: List[_]*): Boolean = data.forall(dataIsNumeric)

  /** Converts a list of Any into a list of Double.*/
  private def toDoubleList(values: List[_]) = values.asInstanceOf[List[Number]].map(_.doubleValue())

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
case object RootReportSection extends ReportSection { def parent = throw new UnsupportedOperationException("The root has no parent.") }

/** Normal nodes representing report sections. */
case class ReportSectionNode(name: String, description: String, parent: ReportSection) extends ReportSection {
  parent.childs += this
}

//Data views

/** The general data view. */
trait DataView extends ReportNode

/** Display a scatter plot. */
case class ScatterPlotView(xData: List[Double], yData: List[Double], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a histogram. */
case class HistogramView(data: List[Double], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a box plot. */
case class BoxPlotView(data: List[(String, List[Double])], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display a line plot*/
case class LinePlotView(data: List[(String, List[Double])], title: String, caption: String, xLabel: String, yLabel: String, parent: ReportSection) extends DataView

/** Display the results of a statistical test. */
case class StatisticalTestView(firstData: (String, List[Double]), secondData: (String, List[Double]), caption: String, testMethod: Option[TwoPairedStatisticalTest], parent: ReportSection) extends DataView

/** Display the results in a table. */
case class TableView(data: List[List[String]], caption: String, parent: ReportSection) extends DataView