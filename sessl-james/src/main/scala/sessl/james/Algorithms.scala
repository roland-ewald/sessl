package sessl.james

import james.core.experiments.optimization.algorithm.plugintype.OptimizationAlgorithmFactory
import james.core.math.random.generators.lcg.LCGGeneratorFactory
import james.core.math.random.generators.mersennetwister.MersenneTwisterGeneratorFactory
import james.core.math.random.generators.plugintype.RandomGeneratorFactory
import james.core.processor.plugintype.ProcessorFactory
import james.core.util.eventset.plugintype.EventQueueFactory
import james.core.util.eventset.BucketsThresholdEventQueueFactory
import james.core.util.eventset.HeapEventQueueFactory
import james.core.util.eventset.LinkedListEventQueueFactory
import james.core.util.eventset.SimpleEventQueueFactory
import sessl.util.CreatableFromVariables
import sessl.Algorithm
import sessl.EventQueue
import sessl.Optimizer
import sessl.RNG
import sessl.RNGUtils
import sessl.Simulator
import sessl._BucketQueue
import sessl._DirectMethod
import sessl._Heap
import sessl._LCG
import sessl._LinkedList
import sessl._MersenneTwister
import sessl._NextReactionMethod
import sessl._SortedList
import sessl._TauLeaping
import simulator.sr.ssa.drm.DirectReactionProcessorVarAFactory
import simulator.sr.ssa.nrm.NextReactionProcessorVarAFactory
import simulator.sr.ssa.tau.TauLeapingProcessorFactory

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

//Optimizers
trait BasicJamesIIOptimizer extends Optimizer with JamesIIAlgo[OptimizationAlgorithmFactory]

//Event Queues
trait BasicJamesIIEventQueue extends EventQueue with JamesIIAlgo[EventQueueFactory]
case class Heap() extends _Heap with BasicJamesIIEventQueue {
  override def factory = new HeapEventQueueFactory
}
case class SortedList() extends _SortedList with BasicJamesIIEventQueue {
  override def factory = new SimpleEventQueueFactory
}
case class LinkedList() extends _LinkedList with BasicJamesIIEventQueue {
  override def factory = new LinkedListEventQueueFactory
}
case class BucketQueue() extends _BucketQueue with BasicJamesIIEventQueue {
  override def factory = new BucketsThresholdEventQueueFactory
}

//Simulators
trait BasicJamesIISimulator extends Simulator with JamesIIAlgo[ProcessorFactory]

case class DirectMethod() extends _DirectMethod with BasicJamesIISimulator {
  override def factory = new DirectReactionProcessorVarAFactory
}

case class NextReactionMethod(val eventQueue: BasicJamesIIEventQueue = Heap())
  extends CreatableFromVariables[NextReactionMethod] with _NextReactionMethod with BasicJamesIISimulator {
  override def factory = new NextReactionProcessorVarAFactory
}

case class TauLeaping(val epsilon: Double = 0.03, val gamma: Double = 10, val stepNum: Int = 100, val criticalReactionThreshold: Int = 10)
  extends CreatableFromVariables[TauLeaping] with _TauLeaping with BasicJamesIISimulator {
  override def factory = new TauLeapingProcessorFactory
}