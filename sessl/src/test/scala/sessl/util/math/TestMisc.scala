/**
 * *****************************************************************************
 * Copyright 2013 ALeSiA Team
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
 * ****************************************************************************
 */
package sessl.util.math

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.RunWith

/**
 * Tests for miscellaneous math functions.
 *
 * @author Roland Ewald
 */
class TestMisc {

  import Misc._

  @Test def testMSE() {
    assertEquals(9.0, mse(Seq(1, 2, 3), Seq(4.0, 5.0, 6.0)), 1e-06)
    assertEquals(156.5, mse(Seq(18, 14), Seq(6, 1)), 1e-06)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testWrongSize() {
    mse(List(1, 2, 3), List(3, 2))
  }
}