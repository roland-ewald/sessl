package sessl.sbw.algorithms

import edu.caltech.sbw._
import sessl.sbw.BasicSBWSimulator
import sessl.sbw.ModuleHandler
import sessl.sbw.SBWSimulatorDescription

case class GillespieDescription(numOfRows:Integer) extends SBWSimulatorDescription {
  
  override def create() = new GillespieRef(numOfRows)
}

case class GillespieRef(numOfRows:Integer) extends BasicSBWSimulator {
  
  val module = ModuleHandler.getModule("edu.kgi.gillespieDM")
  
  val service:Service = module.findServiceByName("gillespie")
  
  val simulator: GillespieProxy = service.getServiceObject(classOf[GillespieProxy]).asInstanceOf[GillespieProxy]
  
  override def setParameter(name:String, value:Double) {
    simulator.setParameter(name, value)
  }
  
  override def loadModel(sbml:String) {
    simulator.loadSBML(sbml)
  }
  
  override def getVariableNames():Array[String] = {
    return simulator.getNamesOfVariables;
  }
  
  override def simulate(endTime:Double):Array[Array[Double]] = {
    return simulator.simulate(0.0, endTime, numOfRows)
  }
}

trait GillespieProxy {

  def loadSBML(sbml: String)
	
  def getNamesOfVariables:Array[String]
	
  def setParameter(name:String, value:Double)
	
  def simulate(start:Double, end:Double, numOfRows:Int):Array[Array[Double]]
}