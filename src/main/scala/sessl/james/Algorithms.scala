package sessl.james

import james.core.experiments.optimization.algorithm.plugintype.OptimizationAlgorithmFactory
import james.core.math.random.generators.lcg.LCGGeneratorFactory
import james.core.math.random.generators.mersennetwister.MersenneTwisterGeneratorFactory
import james.core.math.random.generators.plugintype.RandomGeneratorFactory
import james.core.processor.plugintype.ProcessorFactory
import sessl.RNGUtils
import sessl._DirectMethod
import sessl._HillClimbing
import sessl._LCG
import sessl._MersenneTwister
import sessl._NextReactionMethod
import sessl._SimulatedAnnealing
import sessl._TauLeaping
import simulator.sr.ssa.drm.DirectReactionProcessorVarAFactory
import simulator.sr.ssa.nrm.NextReactionProcessorVarAFactory
import simulator.sr.ssa.tau.TauLeapingProcessorFactory
import steering.configuration.optimization.evolutionary.BFSHillClimbingOptimizerFactory
import steering.configuration.optimization.simulatedannealing.SimulatedAnnealingOptimizerFactory
import integrationtest.bogus.simulator.BestSimulatorInTheWorldFactory
import sessl.Simulator
import sessl.Optimizer
import sessl.RNG
import sessl.util.CreatableFromVariables
import sessl._Heap
import sessl.EventQueue
import james.core.util.eventset.plugintype.EventQueueFactory
import james.core.util.eventset.HeapEventQueueFactory
import james.core.util.eventset.SimpleEventQueueFactory
import sessl._SortedList
import eventqueues.mlist.MListFactory
import sessl._MList
import sessl._CalendarQueue
import eventqueues.calendarqueue.CalendarQueueFactory
import sessl.Algorithm

/**
 * Defines all James II algorithms that are accessible via sessl.
 *
 * @author Roland Ewald
 */
trait JamesIIAlgo[+T <: Factory] extends Algorithm {
  def factory: T
}

//RNGs
trait SimpleJamesIIRNG extends RNG with JamesIIAlgo[RandomGeneratorFactory] {
  def seed: Long;
}

case class LCG(val seed: Long = RNGUtils.defaultSeed) extends _LCG with SimpleJamesIIRNG {
  override def factory = new LCGGeneratorFactory
}

case class MersenneTwister(val seed: Long = RNGUtils.defaultSeed) extends _MersenneTwister with SimpleJamesIIRNG {
  override def factory = new MersenneTwisterGeneratorFactory
}

//Optimizers - TODO: add parameterization
trait BasicJamesIIOptimizer extends Optimizer with JamesIIAlgo[OptimizationAlgorithmFactory]

case object SimulatedAnnealing extends _SimulatedAnnealing with BasicJamesIIOptimizer {
  override def factory = new SimulatedAnnealingOptimizerFactory
}
case object HillClimbing extends _HillClimbing with BasicJamesIIOptimizer {
  override def factory = new BFSHillClimbingOptimizerFactory
}

//Event Queues
trait BasicJamesIIEventQueue extends EventQueue with JamesIIAlgo[EventQueueFactory]
case object Heap extends _Heap with BasicJamesIIEventQueue {
  override def factory = new HeapEventQueueFactory
}
case object SortedList extends _SortedList with BasicJamesIIEventQueue {
  override def factory = new SimpleEventQueueFactory
}
case object MList extends _MList with BasicJamesIIEventQueue {
  override def factory = new MListFactory
}
case object CalendarQueue extends _CalendarQueue with BasicJamesIIEventQueue {
  override def factory = new CalendarQueueFactory
}

//Simulators
trait BasicJamesIISimulator extends Simulator with JamesIIAlgo[ProcessorFactory]

case object DirectMethod extends _DirectMethod with BasicJamesIISimulator {
  override def factory = new DirectReactionProcessorVarAFactory
}

case class NextReactionMethod(val eventQueue: BasicJamesIIEventQueue = MList)
  extends CreatableFromVariables[NextReactionMethod] with _NextReactionMethod with BasicJamesIISimulator {
  override def factory = new NextReactionProcessorVarAFactory
}

case class TauLeaping(val epsilon: Double = 0.03, val gamma: Double = 10, val numSSASteps: Int = 100, val criticalReactions: Int = 10)
  extends CreatableFromVariables[TauLeaping] with _TauLeaping with BasicJamesIISimulator {
  override def factory = new TauLeapingProcessorFactory
}