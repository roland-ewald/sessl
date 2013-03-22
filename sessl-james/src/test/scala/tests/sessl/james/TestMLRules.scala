package tests.sessl.james

import sessl.execute
import sessl.james.Experiment
import sessl.james.MLRulesReference
import org.junit.Test
import org.junit.Assert._

@Test class TestMLRules {

  @Test def testMLRulesObservation = {
    import sessl._
    import sessl.james._

    var trajectoryB: Option[Trajectory] = None

    execute {
      new Experiment with Observation {
        model = "file-mlrj:/./EndoExoCytosis.mlrj"
        simulator = MLRulesReference()
        stopTime = 0.1
        observe("A", "B")
        observeAt(range(0.0, 0.001, 0.095))
        withRunResult(results => {
          logger.info("Results of A:" + results.trajectory("A"))
          trajectoryB = Some(results.trajectory("B"))
        })
      }
    }

    assertTrue(trajectoryB.isDefined)
    assertEquals(trajectoryB.get.size, 95)
  }

}