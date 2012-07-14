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
package tests.sessl

import org.junit.Test

/**
 * @author Roland Ewald
 */
@Test class MultiSimSystemsExample {

  @Test def reuseDefinitionAcrossMultipleSimSystems() = {

    import sessl._
    trait MyExperimentConfiguration {
      this: AbstractExperiment =>
      //TODO: choose other, won't simulate in J2 (too many entities)
      model = "./BIOMD0000000002.xml"
      stopTime = .01
      //...
    }

    execute(
      new sessl.sbmlsim.Experiment with MyExperimentConfiguration,
      new sessl.james.Experiment with MyExperimentConfiguration)
  }

  @Test def executeMultipleSimSystems() = {
    import sessl._
    execute(
      {
        import sessl.sbmlsim._
        new Experiment {
          model = "./BIOMD0000000002.xml"
          stopTime = .01
          //...
        }
      },
      {
        import sessl.james._
        new Experiment {
          model = "./BIOMD0000000002.xml"
          stopTime = .01
          //...
        }
      })
  }

  @Test def integrateSimSystems() = {
    import sessl._
    import sessl.sbmlsim._

    //This experiment shows how to *combine* features of simulation systems!
    execute {
      new sessl.util.test.sbmlsim.SimpleTestExperiment with sessl.james.Report {
        reportName = "SBMLsimulator Report"
        withRunResult { r =>
          {
            reportSection("Run Number " + r.id) {
              linePlot(r ~ "x", r ~ "y")(title = "Integration Results")
            }
          }
        }
      }
    }

  }

}
