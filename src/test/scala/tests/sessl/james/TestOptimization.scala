package tests.sessl.james

import org.junit.Test
import org.junit.Assert._

/** Tests whether optimization works.
 *  @author Roland Ewald
 */
class TestOptimization {

  @Test def testOptimization() = {

    import sessl._
    import sessl.james._

    val exp = new Experiment with Instrumentation with Optimization {

      model = TestJamesExperiments.testModel
      stopTime = 20.5
      replications = 2
      rng = MersenneTwister()

      optimizeFor("x" ~ "S1", "y" ~ "S0")(results => { results.mean("y") + results.mean("x") }) {
        optimizeOnAllConfigs = false //<- can be left away, the default is false
        optimizer = HillClimbing
        optimize("#species" ~ "numSpecs", range(10, 100))
        startOptimizationWith("#species" <~ 12)
        optStopPolicy = OptMaxTime(seconds = 50) or OptMaxAssignments(2) //<- careful, and/or operators have the same priority, use parentheses!
      }
    }
    execute(exp)
  }
}