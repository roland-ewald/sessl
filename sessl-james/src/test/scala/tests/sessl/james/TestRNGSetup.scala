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
package tests.sessl.james

import org.junit.Test
import org.junit.Assert._

/** Tests whether setting up the RNG works.
 *  @author Roland Ewald
 */
class TestRNGSetup {

  @Test def testRNGSetup() = {

    import james.SimSystem
    import sessl._
    import sessl.james._

    val testSeed: Long = 1234

    def testFunction(rngClassName: String)(x: AnyRef): Unit = {
      assertEquals("The correct RNG shall have be selected.",
        rngClassName, SimSystem.getRNGGenerator.getNextRNG.getClass.getName)
      assertEquals("The correct seed shall be used as the initial seed.", testSeed, SimSystem.getRNGGenerator.getInitialSeed())
    }

    val exp1 = new Experiment {
      model = TestJamesExperiments.testModel
      stopTime = .01
      rng = LCG(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.lcg.LCG].getName))
    }

    val exp2 = new Experiment {
      model = TestJamesExperiments.testModel
      stopTime = .01
      rng = MersenneTwister(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.mersennetwister.MersenneTwister].getName))
    }
    execute(exp1, exp2)
  }
}
