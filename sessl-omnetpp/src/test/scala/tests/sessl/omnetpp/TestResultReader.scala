package tests.sessl.omnetpp

import org.junit.Test
import sessl.omnetpp.ResultReader

/**
 * Tests for the result reader.
 * @author Roland Ewald
 *
 */
@Test class TestResultReader {

  @Test def testResultVectorReading() = {
    ResultReader.readVectorFile("",0)
  }

}