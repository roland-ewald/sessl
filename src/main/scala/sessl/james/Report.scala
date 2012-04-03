package sessl.james
import java.io.File
import james.resultreport.dataview.BoxPlotDataView
import james.resultreport.dataview.HistogramDataView
import james.resultreport.dataview.LineChartDataView
import james.resultreport.dataview.ScatterPlotDataView
import james.resultreport.dataview.StatisticalTestDataView
import james.resultreport.dataview.StatisticalTestDefinition
import james.resultreport.renderer.rtex.RTexResultReportRenderer
import james.resultreport.ResultReport
import james.resultreport.ResultReportGenerator
import james.resultreport.ResultReportSection
import sessl.AbstractInstrumentation
import sessl.AbstractReport
import sessl.BoxPlotView
import sessl.DataView
import sessl.AbstractExperiment
import sessl.ExperimentResults
import sessl.HistogramView
import sessl.LinePlotView
import sessl.ReportNode
import sessl.ReportSection
import sessl.ReportSectionNode
import sessl.ScatterPlotView
import sessl.StatisticalTestView
import james.resultreport.dataview.TableDataView
import sessl.TableView

/** Support for James II report generation.
 *
 *  @author Roland Ewald
 *
 */
trait Report extends AbstractReport {
  this: AbstractExperiment =>

  /** The result data views used in James II. */
  private type JDataView[D] = james.resultreport.dataview.ResultDataView[D]

  override def generateReport(results: ExperimentResults) = {

    val report = new ResultReport(reportName, reportDescription)
    fillReportWithContent(report)

    // Check if directory exists
    val reportDir = new File(reportDirectory)
    if (!reportDir.exists())
      require(reportDir.mkdir(), "Could not create non-existent directory '" + reportDir.getAbsolutePath() + "'")

    // Generate report  
    (new ResultReportGenerator).generateReport(report, new RTexResultReportRenderer, new File(reportDirectory))
  }

  /** Fills the given report with content (by considering the data held in the super trait). */
  private[this] def fillReportWithContent(report: ResultReport) = {
    topmostElements.foreach(x => report.addSection(createSectionFromTopElem(x)))
  }

  /** Creates top-most elements of report (adds dummy sections to data views on top-most level). */
  def createSectionFromTopElem(node: ReportNode): ResultReportSection = node match {
    case section: ReportSectionNode => {
      val resultSection = new ResultReportSection(section.name, section.description)
      section.children.foreach(createRepresentationForElem(resultSection, _))
      resultSection
    }
    case view: DataView => {
      val resultSection = new ResultReportSection("Section for Dataview", "This section is automatically generated. Adding data views to the root section is not supported.")
      createRepresentationForElem(resultSection, view)
      resultSection
    }
    case _ => throw new IllegalArgumentException("Element " + node + " not supported.")
  }

  /** Creates the rest of the report hierarchy recursively. */
  def createRepresentationForElem(parent: ResultReportSection, node: ReportNode): Unit = node match {
    case section: ReportSectionNode => {
      val resultSection = new ResultReportSection(section.name, section.description)
      section.children.foreach(createRepresentationForElem(resultSection, _))
      parent.addSubSection(resultSection)
    }
    case view: DataView => {
      parent.addDataView(createJamesDataView(view))
      val x = 0
    }
    case _ => throw new IllegalArgumentException("Element " + node + " not supported.")
  }

  /** Creates data views. */
  def createJamesDataView(view: DataView): JDataView[_] = {
    import sessl.util.ScalaToJava._
    view match {
      case v: ScatterPlotView => new ScatterPlotDataView(to2DJavaDoubleArray(v.xData, v.yData),
        v.caption, v.title, Array[String](v.xLabel, v.yLabel))
      case v: HistogramView => new HistogramDataView(toDoubleArray(v.data), v.caption, v.title, v.xLabel, v.yLabel)
      case v: BoxPlotView => new BoxPlotDataView(to2DJavaDoubleArray(v.data.map(_._2): _*),
        v.caption, v.title, Array(v.xLabel, v.yLabel), Array(v.data.map(x => x._1): _*))
      case v: LinePlotView => new LineChartDataView(to2DJavaDoubleArray(v.data.map(_._2): _*),
        v.caption, v.title, Array(v.xLabel, v.yLabel), Array(v.data.map(x => x._1).tail: _*))
      case v: StatisticalTestView =>
        val dataPair = new james.core.util.misc.Pair[Array[java.lang.Double], Array[java.lang.Double]](toDoubleArray(v.firstData._2), toDoubleArray(v.secondData._2))
        new StatisticalTestDataView(dataPair, v.caption, v.firstData._1, v.secondData._1, true, true, StatisticalTestDefinition.KOLMOGOROV_SMIRNOV)
      case v: TableView => new TableDataView(to2DTransposedJavaStringArray(v.data: _*), v.caption)
      case _ => throw new IllegalArgumentException("Data view " + view + " not yet supported.")
    }
  }

}
