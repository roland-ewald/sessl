package sessl.reference

import sessl.AbstractInstrumentation
import sessl.AbstractExperiment
import sessl.AbstractDataSink
import sessl.AbstractHypothesis
import sessl.AbstractReport
import sessl._LCG
import sessl._MersenneTwister
import sessl.AbstractOptimization
import sessl._SimulatedAnnealing
import sessl.AbstractParallelExecution
import sessl._DirectMethod
import sessl._NextReactionMethod
import sessl._TauLeaping
import sessl.ExperimentResults
import sessl.util.SimpleInstrumentation

/**
 * Empty entities to easily check whether an experiment specification depends on system-specific
 * configuration and components or not. This is a 'formal' reference implementation, i.e. it provides all entities with
 * correct names and interfaces - but it does not realize any functionality so far.
 *
 * @author Roland Ewald
 */

//Central entities
class EmptyExperiment extends AbstractExperiment with SimpleInstrumentation { def execute() = {}; def basicConfiguration = {} }
class Experiment(modelURI: String) extends EmptyExperiment

//Instrumentation
trait Instrumentation extends AbstractInstrumentation {
  this: AbstractExperiment =>
}

//Data sinks
trait DataSink extends AbstractDataSink {
  this: Instrumentation =>
}

//Optimization
trait Optimization extends AbstractOptimization {
  this: AbstractExperiment with AbstractInstrumentation =>
}

//Parallel execution
trait ParallelExecution extends AbstractParallelExecution {
  override def configureParallelExecution(threads: Int) = {}
}

//Hypothesis tests
trait Hypothesis extends AbstractHypothesis {
  this: Instrumentation =>
}

//Reporting
trait Report extends AbstractReport {
  this: AbstractExperiment with Instrumentation =>
  override def generateReport(results: ExperimentResults) = {}
}

//Algorithms:

//RNGs
case class LCG(val seed: Long = 0) extends _LCG
case class MersenneTwister(val seed: Long = 0) extends _MersenneTwister

//Optimizers
case object SimulatedAnnealing extends _SimulatedAnnealing
case object HillClimbing extends _SimulatedAnnealing

//Simulators
case object DirectMethod extends _DirectMethod
case object NextReactionMethod extends _NextReactionMethod
case object TauLeaping extends _TauLeaping