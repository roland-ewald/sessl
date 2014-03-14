package sessl.sbw.algorithms

import edu.caltech.sbw._
import sessl.sbw.BasicSBWSimulator
import sessl.sbw.ModuleHandler
import sessl.sbw.SBWSimulatorDescription
import java.util.List

case class BiospiceSimDescription extends SBWSimulatorDescription {
  
  override def create() = new BiospiceSimRef
}

case class BiospiceSimRef() extends BasicSBWSimulator {
  
  val module = ModuleHandler.getModule("Jarnac")
  
  val service:Service = module.findServiceByName("biospiceSim")
  
  val simulator: BiospiceSimProxy = service.getServiceObject(classOf[BiospiceSimProxy]).asInstanceOf[BiospiceSimProxy]
  
  override def setParameter(name:String, value:Double) {
    simulator.setValue(name, value)
  }
  
  override def loadModel(sbml:String) {
    simulator.loadSBMLString(sbml)
  }
  
  override def getVariableNames():Array[String] = {
    var result:Array[String] = new Array[String](1)
    return simulator.getSpeciesNames.toArray(result)
  }
  
  override def simulate(startTime:Double, endTime:Double):Array[Array[Double]] = {
    simulator.setTimeStart(startTime)
    simulator.setTimeEnd(endTime)
    return simulator.simulate()
  }
}

trait BiospiceSimProxy {

  def loadSBMLString(sbml: String)
	
  def getSpeciesNames():List[_]
  
  def setValue(name:String, value:Double)
  
  def setTimeStart(time:Double)

  def setTimeEnd(time:Double)
	
  def simulate():Array[Array[Double]]
}