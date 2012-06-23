package tests.sessl.omnetpp

import org.junit.Test
import sessl.omnetpp.ResultReader

/** Tests for the result reader.
 *  @author Roland Ewald
 *
 */
@Test class TestResultReader {

  @Test def testResultVectorReading(): Unit = {
    ResultReader.readVectorFile("./omnetpp-samples/", 0)
  }

  @Test def testResultScalarReading(): Unit = {
    ResultReader.readScalarFile("./omnetpp-samples/", 0)
  }

}