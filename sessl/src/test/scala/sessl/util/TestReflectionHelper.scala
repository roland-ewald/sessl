package sessl.util

import org.junit.Test
import org.junit.Assert._


@Test class TestReflectionHelper {

  case class TestExample(val x: Int = 1, val y: Double = 1.0)

  @Test def testDynamicMethodCall() {
    println(ReflectionHelper.caseClassConstrArgs(new TestExample()))
  }

}