/**
 * *****************************************************************************
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
 * ****************************************************************************
 */
package sessl.james

import org.jamesii.core.experiments.optimization.algorithm.plugintype.OptimizationAlgorithmFactory
import org.jamesii.core.math.random.generators.lcg.LCGGeneratorFactory
import org.jamesii.core.math.random.generators.mersennetwister.MersenneTwisterGeneratorFactory
import org.jamesii.core.math.random.generators.plugintype.RandomGeneratorFactory
import org.jamesii.core.processor.plugintype.ProcessorFactory
import org.jamesii.core.util.eventset.BucketsThresholdEventQueueFactory
import org.jamesii.core.util.eventset.HeapEventQueueFactory
import org.jamesii.core.util.eventset.LinkedListEventQueueFactory
import org.jamesii.core.util.eventset.SimpleEventQueueFactory
import org.jamesii.core.util.eventset.plugintype.EventQueueFactory
import sessl.Algorithm
import sessl.EventQueue
import sessl.optimization.Optimizer
import sessl.RNG
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
import sessl.util.CreatableFromVariables
import simulator.sr.ssa.drm.DirectReactionProcessorVarAFactory
import simulator.sr.ssa.nrm.NextReactionProcessorVarAFactory
import simulator.sr.ssa.tau.TauLeapingProcessorFactory
import simulator.pi.channelfocused.ChannelFocusedProcessorFactory
import simulator.pi.reaction.plugintype.ReactionFactory
import sessl.RNGUtils
import simulator.stopi.reaction.next.NextReactionMethodFactory
import simulator.pi.popfocused.PopulationFocusedProcessorFactory
import simulator.pi.comfocused.CommunicationFocusedProcessorFactory
import simulator.srs.ssa.nsm.NSMProcessorFactory
import simulator.pdevsflatsequential.FlatSequentialProcessorFactory
import org.jamesii.simulator.mlrules.population.reference.MLRulesPopulationProcessorFactory
import org.jamesii.core.processor.IProcessor

/**
 * Defines all James II algorithms that are accessible via sessl.
 *
 * @author Roland Ewald
 */
trait JamesIIAlgo[T] extends Algorithm {

  def factory: T

  /** Override this to account for legacy non-standard parameter block names for sub-entities. */
  def customBlockName(entryName: String): Option[String] = None
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

//SR
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

//SRS
case class NextSubvolumeMethod(val EventQueue: String = Heap().factory.getClass().getCanonicalName())
  extends CreatableFromVariables[NextSubvolumeMethod] with BasicJamesIISimulator {
  override def factory = new NSMProcessorFactory
}

//StoPi
case class NextReactionMethodType() extends JamesIIAlgo[ReactionFactory] {
  override def factory = new NextReactionMethodFactory
}

case class PopulationFocusedSimulator(val EventQueue: String = Heap().factory.getClass().getCanonicalName(),
  val ReactionType: String = NextReactionMethodType().factory.getClass().getCanonicalName())
  extends CreatableFromVariables[PopulationFocusedSimulator] with BasicJamesIISimulator {
  override def factory = new PopulationFocusedProcessorFactory
}

case class CommunicationFocusedSimulator(val EventQueue: String = Heap().factory.getClass().getCanonicalName(),
  val ReactionType: String = NextReactionMethodType().factory.getClass().getCanonicalName())
  extends CreatableFromVariables[CommunicationFocusedSimulator] with BasicJamesIISimulator {
  override def factory = new CommunicationFocusedProcessorFactory
}

case class ChannelFocusedSimulator(val EventQueue: String = Heap().factory.getClass().getCanonicalName(),
  val ReactionType: String = NextReactionMethodType().factory.getClass().getCanonicalName())
  extends CreatableFromVariables[ChannelFocusedSimulator] with BasicJamesIISimulator {
  override def factory = new ChannelFocusedProcessorFactory
}

//PDEVS
case class PDEVSFlatSequential(val eventqueue: String = Heap().factory.getClass().getCanonicalName())
  extends CreatableFromVariables[PDEVSFlatSequential] with BasicJamesIISimulator {
  override def factory = new FlatSequentialProcessorFactory
}

//ML-Rules
case class MLRulesReference() extends BasicJamesIISimulator {
  override def factory = new MLRulesPopulationProcessorFactory
}
