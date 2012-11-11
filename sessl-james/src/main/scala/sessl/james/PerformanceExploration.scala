/*******************************************************************************
 * Copyright 2012 Roland Ewald
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package sessl.james
import org.jamesii.simspex.exploration.simple.SimpleSimSpaceExplorer
import org.jamesii.core.experiments.steering.IExperimentSteerer
import org.jamesii.core.experiments.steering.SteeredExperimentVariables
import org.jamesii.core.experiments.variables.ExperimentVariables
import sessl.util.AlgorithmSet
import org.jamesii.core.experiments.steering.ExperimentSteererVariable
import org.jamesii.core.processor.plugintype.ProcessorFactory
import org.jamesii.core.experiments.variables.modifier.SequenceModifier

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
