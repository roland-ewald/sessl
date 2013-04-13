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

  /** Test experiment for optimization. */
  class TestExperiment extends Experiment with Observation with ParallelExecution {
    model = "java://examples.sr.LinearChainSystem"
    stopTime = 1.0
    replications = 2
    observe("x" ~ "S0", "y" ~ "S1")
    observeAt(0.8)
  }

  /** Test execution with a simple optimization problem. */
  @Test def testSimpleOptScheme() = {

    import sessl.optimization._
    import sessl.opt4j._

    var executionDone = false

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
    } using {
      new Opt4JSetup {
        param("unused", List("a", "b", "c")) //This should provoke a warning
        param("p", 1, 1, 15)
        param("n", 10000, 100, 15000)
        optimizer = optAlgos.head
        withOptimizationResults { _ => executionDone = true }
      }
    }
    assertTrue("Execution should complete successfully.", executionDone)
  }

  /** Test optimization with multi-dimensional objective function and multiple optimization algorithms.*/
  @Test def testMultiParamOpt() = {

    import sessl.optimization._
    import sessl.opt4j._

    var resultsPerAlgo = ListBuffer[(Opt4JAlgorithm, OptimizationParameters)]()

    optAlgos.foreach { optAlgo =>
      optimize(MultiObjective(("x", max), ("y", max))) { (params, objective) =>
        execute {
          new TestExperiment {
            set("propensity" <~ params("p"))
            set("numOfInitialParticles" <~ params("n"))
            withReplicationsResult(results => {
              objective("x") <~ results.mean("x")
              objective("y") <~ results.min("y")
            })
          }
        }
      } using {
        new Opt4JSetup {
          param("p", 1, 1, 15)
          param("n", 10000, 100, 15000)
          optimizer = optAlgo
          withOptimizationResults { optResults =>
            resultsPerAlgo += ((optAlgo, optResults.head._1))
          }
        }
      }
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
    } using {
      new Opt4JSetup {
        param("p", 1, 1, 15)
        param("n", 10000, 100, 15000)
        optimizer = RandomSearch(iterations = iterationCount, batchsize = evalsPerIteration)
        //Available event handlers to be tested:         
        withIterationResults { iterationResults += _ }
        withOptimizationResults { optimizationResults += _ }
        afterEvaluation { (_, _) => evaluationCounter += 1 }
        afterIteration { _ => iterationCounter += 1 }
        afterOptimization { _ => optimizationCounter += 1 }
      }
    }
    assertEquals("Event handling w.r.t. overall results is only called once.", 1, optimizationCounter)
    assertEquals(optimizationCounter, optimizationResults.length)
    assertEquals("First iteration is ignored, so per-iteration event handling should be called n-1 times.", iterationCount - 1, iterationCounter)
    assertEquals(iterationCounter, iterationResults.length)
    assertEquals((iterationCount - 1) * evalsPerIteration, evaluationCounter)
  }
}