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
    val resultData = ResultReader.readVectorFile("./omnetpp-samples/", 0)

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
    val resultData = ResultReader.readScalarFile("./omnetpp-samples/", 0)
    assertEquals(2L, resultData("..v0"))
    assertEquals(10457L, resultData("ClosedQueueingNetA.queue[3].out.channel/messages:count"))
  }

}