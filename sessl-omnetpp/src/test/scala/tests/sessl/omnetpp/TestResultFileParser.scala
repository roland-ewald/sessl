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
package tests.sessl.omnetpp

import java.io.FileReader
import org.junit.Assert._
import org.junit.Test
import sessl.omnetpp.ResultFileParser
import junit.framework.TestCase
import sessl.util.Logging

/**
 * Tests for ResultFileParser.
 *
 * @author Roland Ewald
 *
 */
@Test class TestResultFileParser extends Logging {

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
      logger.error(testParserWithString(s).toString)
      fail("Could not parse string: " + s)
    })

  def testParserWithString(content: String) = parser.parseAll(parser.file, content)

  @Test def parserTestSampleFiles = {
    testFiles.par.map(testFileDirectory + _).filterNot(parser.parse(_).successful).foreach(
      fileName => {
        logger.error((parser.parse(fileName)).toString)
        fail("Could not parse file " + fileName)
      })
  }
}
