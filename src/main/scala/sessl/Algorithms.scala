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

//Statistical Testing
trait StatisticalTest extends Algorithm
trait TwoPairedStatisticalTest extends StatisticalTest

trait _KolmogorovSmirnov extends TwoPairedStatisticalTest