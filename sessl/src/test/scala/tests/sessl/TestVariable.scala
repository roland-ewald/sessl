package tests.sessl

import org.junit.Test
import junit.framework.TestCase
import junit.framework.Assert._

import sessl._

/**
 * Testing the generic variable support.
 *
 * @see sessl.Variable
 *
 * @author Roland Ewald
 *
 */
@Test class TestVariable {

  val expectedIntRangeElements = 10
  val expectedDoubleRangeElements = 19

  @Test def testVarRangeToList() = {

    val intRange = { "" <~ range(1, 1, 10) }.asInstanceOf[VarRange[Int]].toList
    assertEquals(expectedIntRangeElements, intRange.length)
    assertEquals(1, intRange(0))
    assertEquals(10, intRange(expectedIntRangeElements - 1))

    assertEquals(expectedDoubleRangeElements, { "doubleRange" <~ range(1, 0.5, 10) }.asInstanceOf[VarRange[Double]].toList.length)
    assertEquals(expectedIntRangeElements, { "reverseIntRange" <~ range(10, -1, 1) }.asInstanceOf[VarRange[Int]].toList.length)

    val reverseDoubleRange = { "doubleRange" <~ range(10, -.5, 1) }.asInstanceOf[VarRange[Double]].toList
    assertEquals(expectedDoubleRangeElements, reverseDoubleRange.length)
    assertEquals(10., reverseDoubleRange(0))
    assertEquals(1., reverseDoubleRange(expectedDoubleRangeElements - 1))

    assertEquals("When lower and upper bound are the same, one element is returned.", 1, { "doubleRange" <~ range(1, 0.5, 1) }.asInstanceOf[VarRange[Double]].toList.length)
  }

  @Test def testVarRangesWithStrings() = {
    val ranges = List(range("%d", 1, 1, 10), range("%f", 1.0, 1.0, 10.0), range("%d", 1, 10))
    ranges.foreach(r => assertTrue(r.forall(!_.isEmpty) && r.length == 10))
  }

}