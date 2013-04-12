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
package sessl.opt4j

import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.optimizers.de.DifferentialEvolutionModule
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.optimizers.rs.RandomSearchModule
import org.opt4j.optimizers.sa.SimulatedAnnealingModule

import sessl.optimization._EvolutionaryAlgorithm
import sessl.optimization._RandomSearch
import sessl.optimization._SimulatedAnnealing

/**
 * Common interface of all optimization algorithms in Opt4J.
 * Their parameters and default values as taken from the original source code.
 */
trait Opt4JAlgorithm {

  /**
   * Factory method.
   * @return fully-configured optimizer
   */
  def create: OptimizerModule
}

/**
 * Represents an evolutionary algorithm.
 * @see EvolutionaryAlgorithmModule
 * @param generations number of generations
 * @param alpha number of individuals per generation
 * @param mu number of parents per generation
 * @param lambda number of offspring per generation
 * @param rate rate of the crossover operation
 */
case class EvolutionaryAlgorithm(val generations: Int = 1000, val alpha: Int = 100, val mu: Int = 25,
  val lambda: Int = 25, val rate: Double = 0.95) extends Opt4JAlgorithm with _EvolutionaryAlgorithm {
  override def create = {
    val rv = new EvolutionaryAlgorithmModule
    rv.setGenerations(generations)
    rv.setAlpha(alpha)
    rv.setMu(mu)
    rv.setLambda(lambda)
    rv.setCrossoverRate(rate)
    rv
  }
}

/**
 * Represents algorithm for differential evolution.
 * <b>Caution: seems to only support real-valued parameters so far.</b>
 * @see DifferentialEvolutionModule
 * @param generations the number of generations
 * @param alpha number of individuals per generation
 * @param scalingFactor the scaling factor
 */
case class DifferentialEvolution(val generations: Int = 1000, val alpha: Int = 100,
  val scalingFactor: Double = 0.5) extends Opt4JAlgorithm with _EvolutionaryAlgorithm {
  override def create = {
    val rv = new DifferentialEvolutionModule
    rv.setGenerations(generations)
    rv.setAlpha(alpha)
    rv.setScalingFactor(scalingFactor)
    rv
  }
}

/**
 * Represents an optimizer based on simulated annealing.
 * @see SimulatedAnnealingModule
 * @param iterations the number of iterations
 */
case class SimulatedAnnealing(val iterations: Int = 100000) extends Opt4JAlgorithm with _SimulatedAnnealing {
  override def create = {
    val rv = new SimulatedAnnealingModule
    rv.setIterations(iterations)
    rv
  }
}

/**
 * Represents an optimizer using random search.
 * @see RandomSearchModule
 * @param iterations the number of iterations
 * @param batchsize the batch size (number of replications for the randomly sampled individual of the given iteration)
 */
case class RandomSearch(val iterations: Int = 1000, val batchsize: Int = 25) extends Opt4JAlgorithm with _RandomSearch {
  override def create = {
    val rv = new RandomSearchModule
    rv.setIterations(iterations)
    rv.setBatchsize(batchsize)
    rv
  }
}