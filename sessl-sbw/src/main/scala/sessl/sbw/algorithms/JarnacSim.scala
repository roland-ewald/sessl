package sessl.sbw.algorithms

import edu.caltech.sbw._
import sessl.sbw.BasicSBWSimulator
import sessl.sbw.ModuleHandler
import sessl.sbw.SBWSimulatorDescription

case class JarnacSimDescription(gillespie:Boolean) extends SBWSimulatorDescription {
  
  override def create() = new JarnacSimRef(gillespie)
}

case class JarnacSimRef(gillespie:Boolean) extends BasicSBWSimulator {
  
  val module = ModuleHandler.getModule("Jarnac")
  
  val service:Service = module.findServiceByName("sim")
  
  val simulator: JarnacSimProxy = service.getServiceObject(classOf[JarnacSimProxy]).asInstanceOf[JarnacSimProxy]
  
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
    simulator.setTimeStart(0.0)
    simulator.setTimeEnd(endTime)
    if (gillespie) {
      return simulator.gillespie
    }
    return simulator.simulate()
  }
}

trait JarnacSimProxy {

  def loadSBML(sbml: String)
	
  def getNamesOfVariables:Array[String]
  
  def getAllLocalParameterNames():Array[String]
	
  def setParameter(name:String, value:Double)
  
  def setTimeStart (time:Double)

  def setTimeEnd (time:Double)
	
  def simulate():Array[Array[Double]]
  
  def gillespie():Array[Array[Double]]
}