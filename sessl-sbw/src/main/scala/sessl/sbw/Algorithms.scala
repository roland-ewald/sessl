package sessl.sbw

import sessl.Simulator
import sessl.util.CreatableFromVariables
import edu.caltech.sbw._


/** Super type of all ODE solvers provided by SBMLsimulator. */
trait BasicSBWSimulator extends Simulator {

  var module:Module = null
  
  /** Create solver and set its step size. */
  def create(): SBWProxySimulator 
  
  def shutDownModule() {
    if (module == null) {
      // TODO: add to logging
      println("Warning no module found to shutdown!")
    } else {
      module.shutdown()
    }
  }
}

case class Gillespie() extends BasicSBWSimulator {
  override def create() = {
    module = SBW.getModuleInstance("edu.kgi.gillespieDM")
    val service:Service = module.findServiceByName("gillespie")
    service.getServiceObject(classOf[SBWProxySimulator]).asInstanceOf[SBWProxySimulator]
  }
}