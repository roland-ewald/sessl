package sessl.omnetpp

import java.io.FileReader

import scala.util.parsing.combinator._

/** Parser for '.sca' and '.vec' result files, as produced by OMNeT++.
 *
 *  Note that index files (.vci) hold additional information that is not yet covered, so that they cannot be parsed with this parser.
 *
 *  @see Appendix (ch. 25) in OMNeT++ Manual (http://omnetpp.org/doc/omnetpp/manual/usman.html).
 *
 *  @author Roland Ewald
 */
class ResultFileParser extends JavaTokenParsers {

  /** End-of-line segments the input. */
  def eol: Parser[Any] = """(\r?\n)+""".r
  /** Do not consider end of line as whitespace. */
  override val whiteSpace = """[ \t]+""".r
  /** In this format, floats _always_ need to have a '.'. */
  override def floatingPointNumber = """-?(\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r

  /** Basic values. */
  def string = "[-.$:=()\\[\\]#A-Za-z0-9]*".r
  def long = wholeNumber ^^ (_.toLong)
  def double = floatingPointNumber ^^ (_.toDouble)
  def numericValue = double | long
  def value = stringLiteral | string | numericValue

  /** OMNeT++ identifiers. */
  def parameterNamePattern = "[*.A-Za-z0-9]*".r
  def moduleName = string
  def scalarName = string
  def vectorName = string
  def columnSpec = "[ETV]+".r
  def vectorId = long

  /** Possible line formats. */
  def versionEntry = ("version" ~ long) ^^ (x => VersionEntry(x._2))
  def runEntry = "run" ~ string
  def attributeEntry = "attr" ~ ident ~ value
  def paramEntry = "param" ~ parameterNamePattern ~ value
  def scalarEntry = ("scalar" ~ moduleName ~ scalarName ~ numericValue) ^^ (x => ScalarDataEntry(x._1._1._2, x._1._2, x._2))
  def vectorEntry = ("vector" ~ vectorId ~ moduleName ~ vectorName ~ opt(columnSpec)) ^^ (x => VectorEntry(x._1._1._1._2, x._1._1._2, x._1._2, x._2))
  def vectorDataEntry = (vectorId ~ rep(numericValue)) ^^ (x => VectorDataEntry(x._1, x._2))

  /** Line and file structure. */
  def line = (
    vectorDataEntry
    | vectorEntry
    | runEntry
    | attributeEntry
    | paramEntry
    | scalarEntry
    | versionEntry) <~ opt(eol)

  def file = rep(line)

  /** Parse a whole file. */
  def parse(fileName: String) = parseAll(file, new FileReader(fileName))
}

/** Super trait for all result data to be considered. */
trait ResultElement {
  /** The character in the vector format string that represents the time column. */
  val timeCharacter = 'T'
  /** The character in the vector format string that represents the value column. */
  val valueCharacter = 'V'
  /** The character in the vector format string that represents the event counter column. */
  val eventCountCharacter = 'E'
  /** (T)ime-(V)alue is the default vector format. 'ETV' is also common. */
  val defaultVectorFormat = timeCharacter.toString + valueCharacter
  /** The separator between module and scalar/vector name. */
  val defaultModuleNameSeparator = '.'
}

/** The version entry in each file. */
case class VersionEntry(version: Long) extends ResultElement {
  /** The supported version. */
  val supportedVersion = 2
  /** Checks whether this version is supported. */
  def isSupportedVersion = version == supportedVersion
}

/** Registration data for a vector. */
case class VectorEntry(id: Long, moduleName: String, vectorName: String, vectorFormat: Option[String]) extends ResultElement {
  /** The format string for this vector. */
  val formatString = vectorFormat.getOrElse(defaultVectorFormat)
  /** The full name of the vector. */
  val name = moduleName + defaultModuleNameSeparator + vectorName

  // Both time and value are required:
  require(formatString.indexOf(timeCharacter) >= 0 && formatString.indexOf(valueCharacter) >= 0,
    "Vector '" + vectorName + "' in module '" + moduleName + "' with id '" + id + "' needs to specify both time and value.")

  /** The index at which the current time is written. */
  lazy val timeIndex = formatString.indexOf(timeCharacter)
  /** The index at which the current value is written. */
  lazy val valueIndex = formatString.indexOf(valueCharacter)
  /** The index at which the current event count is written. */
  lazy val eventCountIndex = formatString.indexOf(eventCountCharacter)
}

/** Holds vector data. */
case class VectorDataEntry(id: Long, values: List[AnyVal]) extends ResultElement {
  /** Get the time of this entry. */
  def time(v: VectorEntry): Double = values(v.timeIndex).asInstanceOf[Number].doubleValue
  /** Get the value of this entry. */
  def value(v: VectorEntry): AnyVal = values(v.valueIndex)
  def eventCount(v: VectorEntry): Long = {
    require(v.eventCountIndex >= 0, "The vector with id '" + v.id + "' does not contain an event counter.")
    values(v.eventCountIndex).asInstanceOf[Long]
  }
}

/** Holds scalar data. */
case class ScalarDataEntry(moduleName: String, scalarName: String, value: Any) extends ResultElement {
  /** The name under which the data can be accessed by the user. */
  val name = moduleName + defaultModuleNameSeparator + scalarName
}