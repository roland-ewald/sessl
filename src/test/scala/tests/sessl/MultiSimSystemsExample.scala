package tests.sessl
import org.junit.Test

/** @author Roland Ewald
 */
@Test class MultiSimSystemsExample {

  @Test def reuseDefinitionAcrossMultipleSimSystems() = {

    import sessl._
    trait MyExperimentConfiguration {
      this: AbstractExperiment =>
      //TODO: choose other, won't simulate in J2 (too many entities)
      model = "./BIOMD0000000002.xml"
      stopTime = .01
      //...
    }

    execute(
      new sessl.sbmlsim.Experiment with MyExperimentConfiguration,
      new sessl.james.Experiment with MyExperimentConfiguration)
  }

  @Test def executeMultipleSimSystems() = {
    import sessl._
    execute(
      {
        import sessl.sbmlsim._
        new Experiment {
          model = "./BIOMD0000000002.xml"
          stopTime = .01
        }
      },
      {
        import sessl.james._
        new Experiment {
          model = "./BIOMD0000000002.xml"
          stopTime = .01
        }
      })
  }

}