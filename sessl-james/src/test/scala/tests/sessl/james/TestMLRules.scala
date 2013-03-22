package tests.sessl.james

import sessl.execute
import sessl.james.Experiment
import sessl.james.MLRulesReference
import org.junit.Test

@Test class TestMLRules {

  @Test def testMLRulesObservation = {
    import sessl._
    import sessl.james._

    execute {
      new Experiment with Observation {
        model = "file-mlrj:/./EndoExoCytosis.mlrj"
        simulator = MLRulesReference()
        stopTime = 0.1
        observe("A") //B, Vesicle, ...?
        observeAt(range(0, 0.001, 0.095))
      }
    }
  }

}