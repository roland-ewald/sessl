package tests.sessl.omnetpp

import org.junit.Test
import org.junit.Assert._
import sessl.omnetpp.ResultReader

/** Tests for the result reader.
 *  @author Roland Ewald
 *
 */
@Test class TestResultReader {

  @Test def testResultVectorReading(): Unit = {
    val resultData = ResultReader.readVectorFile("./omnetpp-samples/", 0)
    for (vectorId <- Range(0, 5)) {
      val data = resultData(vectorId)
      val metaData = data._1 
      assertEquals("ETV", metaData.formatString)
      println(data._2.mkString("\n"))
    }
  }

  @Test def testResultScalarReading(): Unit = {
    val resultData = ResultReader.readScalarFile("./omnetpp-samples/", 0)
    assertEquals(2L, resultData("..v0"))
    assertEquals(10457L, resultData("ClosedQueueingNetA.queue[3].out.channel.messages:count"))
  }

}