package sessl.sbw.algorithms

import sessl.sbw.BasicSBWSimulator
import sessl.sbw.SBWSimulatorDescription
import edu.caltech.sbw.Service
import sessl.sbw.ModuleHandler
import java.util.List

case class RoadRunnerSimDescription extends SBWSimulatorDescription {
  
  override def create() = new RoadRunnerSimRef
}

case class RoadRunnerSimRef extends BasicSBWSimulator {
  
  val module = ModuleHandler.getModule("edu.kgi.roadRunner")
  
  val service:Service = module.findServiceByName("sim")
  
  val simulator: RoadRunnerSimProxy = service.getServiceObject(classOf[RoadRunnerSimProxy]).asInstanceOf[RoadRunnerSimProxy]
  
  override def setParameter(name:String, value:Double) {
    simulator.setValue(name, value)
  }
  
  override def loadModel(sbml:String) {
    simulator.loadSBML(sbml)
  }
  
  override def getVariableNames():Array[String] = {
    var result:Array[String] = new Array[String](1)
    return simulator.getFloatingSpeciesNames.toArray(result)
  }
  
  override def simulate(startTime:Double, endTime:Double):Array[Array[Double]] = {
    simulator.setTimeStart(startTime)
    simulator.setTimeEnd(endTime)
    return simulator.simulate()
  }
}

trait RoadRunnerSimProxy {

  def loadSBML(sbml: String)
	
  def getFloatingSpeciesNames():List[_]
  
  def setValue(name:String, value:Double)
  
  def setTimeStart (time:Double)

  def setTimeEnd (time:Double)
	
  def simulate():Array[Array[Double]]
}