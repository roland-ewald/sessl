package tests.sessl.omnetpp

import java.io.FileReader
import org.junit.Assert._
import org.junit.Test
import sessl.omnetpp.ResultFileParser
import junit.framework.TestCase

/**
 * Tests for ResultFileParser.
 *
 * @author Roland Ewald
 *
 */
@Test class TestResulFileParser {

  /** The parser to be tested. */
  val parser = new ResultFileParser

  /** The directory of the test files. */
  val testFileDirectory = "./omnetpp-samples/results/"

  /** The files to be tested. */
  val testFiles = Seq("General-0.vec", "General-0.sca")

  @Test def simpleParserTest = {
    testParserWithStrings(
      "version 1\nrun General-0-20120622-10:25:30-7500\n",
      "run General-0-20120622-10:25:30-7500\n",
      "attr inifile omnetpp.ini\n" + "attr iterationvars \"\"\n" + "attr iterationvars2 $repetition=0\n" + "attr measurement \"\"",
      "param **.gen.sendIaTime  exponential(0.01)\nparam **.gen.msgLength   10\nparam **.fifo.bitsPerSec 1000",
      "scalar .  v0  2\nscalar .  v1  2",
      "vector 0  ClosedQueueingNetA.queue[0]  queueLength  ETV",
      "0 923 346588 0 147250 0 35996.998173643918 11620 0 10 16908 44594")
  }

  def testParserWithStrings(content: String*) =
    content.filterNot(testParserWithString(_).successful).foreach(s => {
      println(testParserWithString(s))
      fail("Could not parse string: " + s)
    })

  def testParserWithString(content: String) = parser.parseAll(parser.file, content)

  @Test def parserTestSampleFiles = {
    testFiles.par.map(testFileDirectory + _).filterNot(testParserWithFile(_).successful).foreach(
      fileName => {
        println(testParserWithFile(fileName))
        fail("Could not parse file " + fileName)
      })
  }
  def testParserWithFile(fileName: String) = parser.parseAll(parser.file, new FileReader(fileName))

}