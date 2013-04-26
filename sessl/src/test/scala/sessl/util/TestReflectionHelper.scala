/**
 * *****************************************************************************
 * Copyright 2012 - 2013 Roland Ewald
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
 * ***************************************************************************
 */
package sessl.util

import org.junit.Test
import org.junit.Assert._
import org.junit.Assert
import ReflectionHelper._

case class TestNormalCaseClass(val x: Int = 1, val y: String = "test")

/**
 * Tests for {@link ReflectionHelper}.
 *
 * @see ReflectionHelper
 * @author Roland Ewald
 */
@Test class TestReflectionHelper {

  val testString = "test"

  case class TestInnerCaseClass(val z: Int = 2)

  @Test def testNormalCaseClassConstructorReflection() {
    assertEquals(Seq(("x", 1), ("y", testString)), caseClassConstrArgs(TestNormalCaseClass()).toSeq)
  }

  @Test def testInnerCaseClassConstructorReflection() {
    assertEquals(Seq(("z", 2)), caseClassConstrArgs(TestInnerCaseClass()).toSeq)
  }

  @Test(expected = classOf[ScalaReflectionException])
  def testFunctionInnerCaseClassConstructorReflection() {
    case class TestFunctionInnerCaseClass(val testName: Int = 4, x: String = testString)
    assertEquals(Seq(("testName", 4), "x", testString), caseClassConstrArgs(TestFunctionInnerCaseClass()).toSeq)
  }

}