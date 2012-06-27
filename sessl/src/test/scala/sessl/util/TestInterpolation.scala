package sessl.util

import org.junit.Test
import org.junit.Assert._
import Interpolation._

/**
 * Tests for the interpolation utilities.
 *  @author Roland Ewald
 */
@Test class TestInterpolation {

  @Test(expected = classOf[IllegalArgumentException])
  def testInterpolationPointsWrongRecordedTime() {
    //TODO: Should such a case be supported?
    findInterpolationPoints(List(1), List(1, 1))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInterpolationPointsWrongObsTimes() {
    findInterpolationPoints(List(1, 2), List())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInterpolationPointsWrongLowerBound() {
    findInterpolationPoints(List(1, 2), List(0, 1))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInterpolationPointsWrongUpperBound() {
    findInterpolationPoints(List(1, 2), List(1, 3))
  }

  @Test
  def testFindingInterpolationPoints() {
    val trueInterpPoint = ((0, 1.0), 1.0, (1, 3.0))
    assertEquals("Only one interpolation point shall be found here.",
      List(trueInterpPoint), findInterpolationPoints(List(1, 3), List(1)))

    val otherInterpPoint = findInterpolationPoints(List(1, 3), List(2))(0)
    assertTrue("Lower & upper interval should be the same",
      trueInterpPoint._1 == otherInterpPoint._1 && trueInterpPoint._3 == otherInterpPoint._3)

    val multInterpPoints = findInterpolationPoints(List(1, 3), List(1, 2, 3))
    assertEquals("There should be three interpolation points.", 3, multInterpPoints.size)
    assertTrue("All three points should have the same bounds.",
      multInterpPoints.forall(p => { p._1 == multInterpPoints(0)._1 && p._3 == multInterpPoints(0)._3 }))

    val pointsDiffBounds = findInterpolationPoints(List(1, 2, 3), List(1, 2, 3))
    assertEquals("There should be three interpolation points.", 3, pointsDiffBounds.size)
    assertEquals((0, 1), pointsDiffBounds(0)._1)
    assertEquals(pointsDiffBounds(0)._1, pointsDiffBounds(1)._1)
    assertEquals((1, 2), pointsDiffBounds(0)._3)
    assertEquals(pointsDiffBounds(0)._3, pointsDiffBounds(1)._3)
    assertEquals(((1, 2), 3, (2, 3)), pointsDiffBounds(2))
  }

}