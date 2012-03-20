package sessl.james
import simspex.exploration.simple.SimpleSimSpaceExplorer
import james.core.experiments.steering.IExperimentSteerer
import james.core.experiments.steering.SteeredExperimentVariables
import james.core.experiments.variables.ExperimentVariables
import sessl.util.AlgorithmSet
import james.core.experiments.steering.ExperimentSteererVariable
import james.core.processor.plugintype.ProcessorFactory
import james.core.experiments.variables.modifier.SequenceModifier

/** Support for the performance exploration methods provided by James II.
 *
 *  @author Roland Ewald
 */
trait PerformanceExploration {
  this: Experiment =>

  def configureSimulationSpaceExploration() = {
    // Set up steerer variables
    val steererVars = new SteeredExperimentVariables(classOf[IExperimentSteerer])
    steererVars.setSubLevel(exp.getExperimentVariables())
    val steerers = new java.util.ArrayList[IExperimentSteerer]()

    // Create parameter block list of all setups
    val paramBlockList = new java.util.ArrayList[ParamBlock]()
    ParamBlockGenerator.createParamBlockSet(simulators.asInstanceOf[AlgorithmSet[JamesIIAlgo[Factory]]]).foreach(
      p => paramBlockList.add(new ParamBlock().addSubBl(classOf[ProcessorFactory].getName(), p)))

    // Set up explorer
    val explorer = new SimpleSimSpaceExplorer(paramBlockList)
    explorer.setCalibrator(null)
    explorer.setMaxModelSpaceElems(1)
    explorer.setNumOfReplications(1)
    steerers.add(explorer);
    val newExpVars = new ExperimentVariables()
    newExpVars.setSubLevel(steererVars)
    newExpVars.addVariable(new ExperimentSteererVariable[IExperimentSteerer](
      "SimSpExSteererVar", classOf[IExperimentSteerer], explorer,
      new SequenceModifier[IExperimentSteerer](steerers)));
    exp.setExperimentVariables(newExpVars)
  }

}