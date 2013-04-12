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

import sessl.optimization._EvolutionaryAlgorithm
import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule

/**
 * Common interface of all optimization algorithms.
 */
trait Opt4JAlgorithm {

  /**
   * Factory method.
   *  @return fully-configured optimizer
   */
  def create: OptimizerModule
}

/**
 * Represents an evolutionary algorithm. Parameters and default values as taken from the original source code.
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

