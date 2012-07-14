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
package sessl

/**
 * General support for algorithms.
 *
 * @author Roland Ewald
 *
 */

// This could work like an 'ontology' or 'taxonomy' (additional information/relations can be expressed via traits)
abstract trait Algorithm
abstract trait Approximation

//Random Number Generation
trait RNG extends Algorithm

/** Most RNGs use seeds of type Long. */
abstract class RNGWithLongSeed extends RNG {
  val seed: Long
}

/** Auxiliary methods for Long-seed RNG definitions. */
object RNGUtils {
  def defaultSeed = System.currentTimeMillis
}

trait _LCG extends RNGWithLongSeed
trait _MersenneTwister extends RNGWithLongSeed

//Optimization
trait Optimizer extends Algorithm

trait _SimulatedAnnealing extends Optimizer
trait _HillClimbing extends Optimizer

//Simulation
trait Simulator extends Algorithm

//SSA
trait SSA extends Simulator
trait _DirectMethod extends SSA
trait _NextReactionMethod extends SSA
trait _TauLeaping extends SSA with Approximation

//Event Queues
trait EventQueue extends Algorithm

trait _Heap extends EventQueue
trait _SortedList extends EventQueue
trait _MList extends EventQueue
trait _CalendarQueue extends EventQueue
trait _BucketQueue extends EventQueue
trait _LinkedList extends EventQueue

//Statistical Testing
trait StatisticalTest extends Algorithm
trait TwoPairedStatisticalTest extends StatisticalTest

trait _KolmogorovSmirnov extends TwoPairedStatisticalTest
