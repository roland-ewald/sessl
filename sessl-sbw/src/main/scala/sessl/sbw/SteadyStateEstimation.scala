package sessl.sbw

import sessl.AbstractObservation
import sessl.AbstractExperiment
import sessl.ObservationRunResultsAspect
import sessl.TimeStampedData
import analysis.singlerun.steadystate.estimator.ISteadyStateEstimator
import org.jamesii.core.problemsolver.IProblemSolverState
import java.util.ArrayList
import sessl.DataElemBinding

trait SteadyStateEstimation extends AbstractObservation with SBWAnalysis {
  this: AbstractExperiment =>
    
  private var estimator:ISteadyStateEstimator[_] = null
  
  private var estimatorStates: Map[Integer, Map[String, IProblemSolverState[_,_]]] = Map()
  
  /** The mapping from internal names to sessl names.*/
  private var bindings: Map[String, String] = Map()
  
  def estimateSteadyState(estimator:ISteadyStateEstimator[_], binds:DataElemBinding*) {
    this.estimator = estimator
    for (binding <- binds) {
      bindings += (binding.internalName -> binding.sesslName)
    }
  }

  abstract override def getNextTimeInterval(runId:Integer, stopTime:Double):Double = {
    var varStateMap = estimatorStates.getOrElse(runId, null);
    if (varStateMap == null) {
      varStateMap = Map(); 
      estimatorStates+=(runId -> varStateMap); 
      return stopTime
    }
    var finished:Boolean = true
    for (binding <- bindings) {
      val results = collectResults(runId, true)
      val resultName = binding._1
      val steadyStateName = binding._2
      val trajectory = results.trajectory(resultName)
      val timeSeries = translate(trajectory)
      val state = varStateMap.getOrElse(steadyStateName, estimator.init())
      val resState = estimator.solve(state.asInstanceOf[IProblemSolverState[_,_]], timeSeries).asInstanceOf[IProblemSolverState[_,_]]
      varStateMap+=(steadyStateName -> resState)
      val result = resState.getCurrentResult()
      if (!resState.getNextRequest().asInstanceOf[Boolean]) {
        //TODO return results
      } else {
        finished = false
      }
    }
    if (!finished) {
      return stopTime
    }
    return 0.0;
  }
  
  protected[this] def translate(data:List[TimeStampedData]):java.util.List[Number] = {
    var result:java.util.List[Number] = new ArrayList[Number]
    for (point <- data) {
      result.add(point._2.asInstanceOf[Double])
    }
    return result
  }
}

