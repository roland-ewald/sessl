/**
 * *****************************************************************************
 * Copyright 2013 Roland Ewald
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
package tests.sessl

import org.junit.Test
import org.junit.Test
import org.junit.runner.RunWith
import sessl.opt4j.Opt4JSetup

@Test class TestGenericOptimization {

  @Test def testSimpleOptScheme() = {

    import sessl._
    import sessl.james._
    import sessl.optimization._

    optimize(Objective(max)) { (params, objective) =>
      {
        execute {
          new Experiment with Observation with ParallelExecution {
            model = "java://examples.sr.LinearChainSystem"
            set("propensity" <~ params("p"))
            set("numOfInitialParticles" <~ params("n"))
            stopTime = 1.0
            replications = 2
            observe("x" ~ "S0", "y" ~ "S1")
            observeAt(0.8)
            withReplicationsResult(results => {
              objective <~ results.mean("x")
            })
          }
        }
      }
    } using {
      new Opt4JSetup {
        param("unimportant", List("a", "b", "c"))
        param("p", 1, 1, 15)
        param("n", 10000, 100, 15000)
      }
    }
  }
}