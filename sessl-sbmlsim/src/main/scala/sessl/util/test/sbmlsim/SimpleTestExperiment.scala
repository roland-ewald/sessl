package sessl.util.test.sbmlsim

import sessl.stringToDataElementName
import sessl.stringToVarName
import sessl.sbmlsim.DormandPrince54
import sessl.sbmlsim.Experiment
import sessl.sbmlsim.Observation
import sessl.sbmlsim.ParallelExecution
import sessl.range

/**
 * Simple test experiment.
 * @author Roland Ewald
 */
class SimpleTestExperiment extends Experiment with ParallelExecution with Observation {

  model = "./BIOMD0000000002.xml"
  set("kr_0" <~ 8042)
  scan("kf_2" <~ range(30000, 1000, 34000), "kr_2" <~ (650, 750))
  stopTime = .01
  observe("x" ~ "ILL", "y" ~ "DLL")
  observeAt(range(0, 1e-04, 1e-02))

  simulators <~ (DormandPrince54() scan { "stepSize" <~ (1e-06, 2e-06) })
}