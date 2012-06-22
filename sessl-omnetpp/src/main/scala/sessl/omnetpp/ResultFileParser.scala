package sessl.omnetpp

import scala.util.parsing.combinator._

/**
 * Parser for '.sca' and '.vec' result files, produced by OMNeT++.
 *
 * Note that index files (.vci) hold additional information that is not yet covered, so that they cannot be parsed with this parser.
 *
 * @see Appendix (ch. 25) in OMNeT++ Manual (http://omnetpp.org/doc/omnetpp/manual/usman.html).
 *
 * @author Roland Ewald
 */
class ResultFileParser extends JavaTokenParsers {

  /** Consider end of line. */
  override val whiteSpace = """[ \t]+""".r
  def eol: Parser[Any] = """(\r?\n)+""".r

  /** Basic values. */
  def string = "[-.$:=()\\[\\]#A-Za-z0-9]*".r
  def int = wholeNumber ^^ (_.toInt)
  def float = floatingPointNumber ^^ (_.toDouble)
  def numericValue = float | int
  def value = stringLiteral | string | numericValue

  /** OMNeT++ identifiers. */
  def parameterNamePattern = "[*.A-Za-z0-9]*".r
  def moduleName = string
  def scalarName = string
  def vectorName = string
  def columnSpec = "[ETV]+".r
  def vectorId = int

  /** Possible line formats. */
  def versionEntry = ("version" ~ int) ^^ (x => VersionEntry(x._2))
  def runEntry = "run" ~ string
  def attributeEntry = "attr" ~ ident ~ value
  def paramEntry = "param" ~ parameterNamePattern ~ value
  def scalarEntry = "scalar" ~ moduleName ~ scalarName ~ value
  def vectorEntry = "vector" ~ vectorId ~ moduleName ~ vectorName ~ opt(columnSpec)
  def vectorDataEntry = vectorId ~ rep(numericValue)

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
}

case class VersionEntry(version: Int)
case class VectorDataEntry(vectorId: Int, values: List[Numeric[_]])