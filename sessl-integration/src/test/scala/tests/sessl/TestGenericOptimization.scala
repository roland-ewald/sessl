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

import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import sessl.execute
import sessl.james.Experiment
import sessl.james.Observation
import sessl.james.ParallelExecution
import sessl.opt4j.EvolutionaryAlgorithm
import sessl.opt4j.RandomSearch
import sessl.opt4j.SimulatedAnnealing
import sessl._
import sessl.james._

/**
 * Tests regarding the generic support for simulation-based optimization (sessl.optimization).
 * The Opt4J binding is used as a concrete implementation.
 *
 * @see AbstractOptimizerSetup
 * @see Opt4JSetup
 *
 * @author Roland Ewald
 *
 */
@Test class TestGenericOptimization {

  /** Optimization algorithms used for testing. */
  val optAlgos = List(
    EvolutionaryAlgorithm(generations = 2, alpha = 10),
    SimulatedAnnealing(iterations = 20),
    RandomSearch(iterations = 20, batchsize = 1))

    

  /** Test execution with a simple optimization problem. */
  @Test def testSimpleOptScheme() = {

    import sessl.optimization._
    import sessl.opt4j._

    var executionDone = false

    maximize { (params, objective) =>
      execute(new TestExperiment {
        set("propensity" <~ params("p"))
        set("numOfInitialParticles" <~ params("n"))
        withReplicationsResult(results => {
          objective <~ results.mean("x")
        })
      })
    } using (new Opt4JSetup {
      param("unused", List("a", "b", "c")) //This should provoke a warning
      param("p", 1, 1, 15)
      param("n", 10000, 100, 15000)
      optimizer = optAlgos.head
      withOptimizationResults { _ => executionDone = true }
    })

    assertTrue("Execution should complete successfully.", executionDone)
  }

  /** Test optimization with multi-dimensional objective function and multiple optimization algorithms.*/
  @Test def testMultiParamOpt() = {

    import sessl.optimization._
    import sessl.opt4j._

    var resultsPerAlgo = ListBuffer[(Opt4JAlgorithm, OptimizationParameters)]()

    optAlgos.foreach { optAlgo =>
      optimize(("x", max), ("y", max)) { (params, objective) =>
        execute(new TestExperiment {
          set("propensity" <~ params("p"))
          set("numOfInitialParticles" <~ params("n"))
          simulator = TauLeaping(epsilon = params.get("eps"))
          withReplicationsResult(results => {
            objective("x") <~ results.mean("x")
            objective("y") <~ results.min("y")
          })
        })
      } using (new Opt4JSetup {
        param("p", 1, 1, 15)
        param("n", 10000, 100, 15000)
        param("eps", 0.001, 0.05)
        optimizer = optAlgo
        withOptimizationResults { optResults =>
          resultsPerAlgo += ((optAlgo, optResults.head._1))
        }
      })
    }
    assertEquals("There should be one result for each algorithm tried.", optAlgos.length, resultsPerAlgo.length)
  }

  /** Test event handling (what to do with the results, and when).*/
  @Test def testEventHandling() = {

    import sessl.optimization._
    import sessl.opt4j._

    val iterationCount = 8
    val evalsPerIteration = 5

    var evaluationCounter = 0
    var iterationCounter = 0
    var optimizationCounter = 0
    val iterationResults = ListBuffer[List[_]]()
    val optimizationResults = ListBuffer[List[_]]()

    maximize { (params, objective) =>
      execute {
        new TestExperiment {
          set("propensity" <~ params("p"))
          set("numOfInitialParticles" <~ params("n"))
          withReplicationsResult(results => {
            objective <~ results.mean("x")
          })
        }
      }
    } using (new Opt4JSetup {
      param("p", 1, 1, 15)
      param("n", 10000, 100, 15000)
      optimizer = RandomSearch(iterations = iterationCount, batchsize = evalsPerIteration)

      //Available event handlers to be tested:         
      withIterationResults { iterationResults += _ }
      withOptimizationResults { optimizationResults += _ }
      afterEvaluation { (_, _) => evaluationCounter += 1 }
      afterIteration { _ => iterationCounter += 1 }
      afterOptimization { _ => optimizationCounter += 1 }
    })

    assertEquals("Event handling w.r.t. overall results is only called once.", 1, optimizationCounter)
    assertEquals(optimizationCounter, optimizationResults.length)
    assertEquals("First iteration is ignored, so per-iteration event handling should be called n-1 times.", iterationCount - 1, iterationCounter)
    assertEquals(iterationCounter, iterationResults.length)
    assertEquals((iterationCount - 1) * evalsPerIteration, evaluationCounter)
  }

  @Test def testGenericOptimizationBibExperiment() {

    import sessl._ // SESSL core
    import sessl.james._ // JAMES II binding
    import sessl.optimization._ // Support for simulation-based optimization
    import sessl.opt4j._ // Opt4J binding

    var maxObjective: Option[Double] = None

    maximize { (params, objective) => //Maximize the following function:
      execute {
        new Experiment with Observation with ParallelExecution with DataSink {

          model = "file-sr:/./SimpleModel.sr" // Basic setup
          stopTime = 100000
          set("r1" <~ params("synthRate"), "r2" <~ params("degradRate"))

          replications = 10 // Setup for stochastic simulation
          rng = MersenneTwister()

          observe("A") // Model instrumentation
          observeAt(10000, 20000, 99900)

          dataSink = MySQLDataSink(schema = "test", password = "") // Data storage

          simulator = DirectMethod() // Configure simulation algorithm to use

          withExperimentResult { results => //Store objective function
            objective <~ results.max("A")
          }
        }
      }
    } using {
      new Opt4JSetup {
        param("synthRate", 1.0, 10.0)
        param("degradRate", 5.0, 15.0)
        optimizer = SimulatedAnnealing(iterations = 20)
        withOptimizationResults { results =>
          maxObjective = Some(results(0)._2.asInstanceOf[SingleObjective].singleValue)
        }
      }
    }

    assertTrue(maxObjective.isDefined)
    assertTrue(maxObjective.get > 0)
  }
}