package sessl.sbw

import sessl.Simulator
import sessl.util.CreatableFromVariables
import edu.caltech.sbw._
import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.HashMap

object ModuleHandler {
  
  var modules = new HashMap[String, Module] with SynchronizedMap[String, Module]
  
  def getModule(name:String):Module = modules.getOrElseUpdate(name, SBW.getModuleInstance(name))
  
  def shutDownModules() = modules.values.map{m:Module => m.shutdown()}
  
}

trait SBWSimulatorDescription extends Simulator {
  
  /** Create solver and set its step size. */
  def create():BasicSBWSimulator
}

/** Super type of all ODE solvers provided by SBMLsimulator. */
trait BasicSBWSimulator extends Simulator {

  def loadModel(sbml:String)
  
  def setParameter(name:String, value:Double)
  
  def getVariableNames():Array[String]
  
  def simulate(endTime:Double):Array[Array[Double]]
}

