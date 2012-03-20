package tests.sessl.james

import org.junit.Test
import org.junit.Assert._

/** Tests execution of an experiment that was described in a paper on the experimentation layer of James II.
 *
 *  R. Ewald, J. Himmelspach, M. Jeschke, S. Leye, and A. M. Uhrmacher,
 *  "Flexible experimentation in the modeling and simulation framework JAMES II-implications for computational systems biology,"
 *  Briefings in Bioinformatics, vol. 11, no. 3, pp. bbp067-300, Jan. 2010.
 *
 *  Available: http://dx.doi.org/10.1093/bib/bbp067
 *
 *  @author Roland Ewald
 */
class TestBiBJamesExperiment {

  @Test def testBriefingsExperiment() = {

    import sessl._
    import sessl.james._

    val exp = new Experiment with Instrumentation with ParallelExecution with DataSink with Optimization {

      //Basic setup
      model = "file-sr:/./SimpleModel.sr"
      stopTime = 100000

      //Experiment design
      optimizeFor("x" ~ "A")(results => results.max("x")) { //<- diff: uses curve fitting with some real data, could be easily provided like this: curveFitting(results.trajectory("x"), realData)
        optStopPolicy = OptMaxAssignments(10) or OptMaxTime(hours = 1) //diff: was 100, but this takes too long for testing, no time constraint! ;-)
        optimize("synthRate" ~ "r1", range(1.0, 10.0))
        optimize("degradRate" ~ "r2", range(5.0, 15.0))
        startOptimizationWith("synthRate" <~ 1.0, "degradRate" <~ 5.0)
        optimizer = HillClimbing //<- diff: SimulatedAnnealing() for James is buggy
      }

      //Setup for stochastic simulation
      replications = 10
      rng = MersenneTwister()

      //Model instrumentation
      observeAtTimes(10000, 20000, 99900) {} //<- diff: 100000 is stop time, but sim end hook is not yet used...

      //Data storage (diff: using DB instead of file-based, as it is already released - which is slightly more complicated to set up)
      dataSink = MySQLDataSink(schema = "test2")

      //Execution
      simulator = DirectMethod
    }
    //execute(exp) //TODO: re-activate after SR model-reader bug is fixed
  }
}