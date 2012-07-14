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

import org.junit.Test
import org.junit.Assert._
import sessl.omnetpp.ResultReader

/**
 * Tests for the result reader.
 *  @author Roland Ewald
 *
 */
@Test class TestResultReader {

  @Test def testResultVectorReading(): Unit = {
    val resultData = ResultReader.readVectorFile("./omnetpp-samples", 0)

    //Checks data for each kind of vector in the test file
    for (vectorId <- Range(0, 5)) {
      val (metaData, rawDataEntries) = resultData(vectorId)
      assertEquals("ETV", metaData.formatString)

      //Checks that all entries are in the correct temporal order
      var previousRawData = rawDataEntries.head
      for (rawData <- rawDataEntries.drop(1)) {
        assertEquals(3, rawData.values.length)
        assertTrue(previousRawData.time(metaData) <= rawData.time(metaData))
        assertTrue(previousRawData.eventCount(metaData) <= rawData.eventCount(metaData))
        previousRawData = rawData
      }
    }
  }

  @Test def testResultScalarReading(): Unit = {
    val resultData = ResultReader.readScalarFile("./omnetpp-samples", 0)
    assertEquals(2L, resultData("..v0"))
    assertEquals(10457L, resultData("ClosedQueueingNetA.queue[3].out.channel.messages:count"))
  }

}
