package tests.sessl.james

import org.junit.Test
import org.junit.Assert._

/** Tests whether setting up the RNG works.
 *  @author Roland Ewald
 */
class TestRNGSetup {

  @Test def testRNGSetup() = {

    import james.SimSystem
    import sessl._
    import sessl.james._

    val testSeed: Long = 1234

    def testFunction(rngClassName: String)(x: AnyRef): Unit = {
      assertEquals("The correct RNG shall have be selected.",
        rngClassName, SimSystem.getRNGGenerator.getNextRNG.getClass.getName)
      assertEquals("The correct seed shall be used as the initial seed.", testSeed, SimSystem.getRNGGenerator.getInitialSeed())
    }

    val exp1 = new Experiment {
      model = TestJamesExperiments.testModel
      stopTime = .01
      rng = LCG(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.lcg.LCG].getName))
    }

    val exp2 = new Experiment {
      model = TestJamesExperiments.testModel
      stopTime = .01
      rng = MersenneTwister(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.mersennetwister.MersenneTwister].getName))
    }
    execute(exp1, exp2)
  }
}