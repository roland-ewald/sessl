package sessl.sbmlsim

import sessl.AbstractExperiment
import org.sbml.jsbml.Model
import org.sbml.jsbml.xml.stax.SBMLReader

/** Encapsulates the SBML simulator core.
 *  @author Roland Ewald
 */
class Experiment extends AbstractExperiment {

  private[this] var model: Option[Model] = None

  def basicConfiguration(): Unit = {
    configureModelLocation()
  }

  def configureModelLocation() = {
    model = Some((new SBMLReader()).readSBML(modelLocation.get).getModel());
    require(model.isDefined, "Reading odel from '" + modelLocation.get + "' failed.")
  }

  def execute(): Unit = {}

}