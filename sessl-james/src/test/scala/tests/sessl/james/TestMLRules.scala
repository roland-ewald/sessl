package tests.sessl.james

import sessl.execute
import sessl.james.Experiment
import sessl.james.MLRulesReference

object TestMLRules extends App {
  
  import sessl._
  import sessl.james._
  
  execute {
    new Experiment {
      model = "file-mlrj:/./EndoExoCytosis.mlrj"
      simulator = MLRulesReference()
      stopTime = 0.1
    }
  }
  

}