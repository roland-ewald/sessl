package tests.sessl.james

import java.io.File

import org.junit.Assert._
import org.junit.Test

import james.core.util.StopWatch
import sessl.james.Experiment
import sessl.util.CreatableFromVariables
import sessl.util.MiscUtils
import sessl.AbstractExperiment
import tests.sessl.TestCounter

import tests.sessl.james.TestJamesExperiments._

/** Some simple tests for sessl.
 *
 *  LOG/ROADMAP
 *
 *  v0.1: Is it possible?
 *  - Basic constructs: parameter scan, mix-in composition: OK
 *
 *  v0.2: Could it be nice to use?
 *  - Support for event handling (run/expDone): OK
 *  - Support for result extraction: OK
 *  - Support for instrumentation: OK
 *  - JamesII-Instrumentation for SR: OK (-> needs to be generalized for other formalisms / use general system by Tobias and Johannes?)
 *
 *  v0.3: Can we do what we did in the Briefings'10-Paper?
 *  [Stubs as a reference implementation, to check whether experiment specifications are (syntactically) sim-system independent?]: OK
 *  - Support setting a random seed: OK
 *  - Support for defining fixed model parameters: OK
 *  - Support for afterReplications event handler: OK
 *  - Support for aggregated results (aggregated by assignment, ie. all replications grouped together) / result.having(varName ==> value) for expresults: OK
 *  - Support for optimization: OK
 *  - Simple support for specifying simulation algorithm(s): OK
 *
 *  v0.4: Can we generate reports?: OK
 *  - Support for histogram, lineplot, boxplot, scatterplot: OK
 *  - Support for *reporting* statistical tests, tables: OK
 *
 *  v0.5: Can we do what FullExploration does?
 *  - Support for stop conditions in Experiment + James II: OK
 *  - Support for replication conditions in Experiment: OK
 *  - Support for replication conditions in James II?: OK (problems in James II)
 *  - Support for defining *sets* of algorithm setups: OK
 *  - Support for translating the defined algorithm sets to James II parameter blocks: OK
 *  - [how to switch to the adaptive runner?: create an execution mode field in parallel execution!]: OK
 *  - use each simulator in case a set of simulation configurations is given: OK
 *  ===================TODO:
 *  - Generalize the way results on the different levels are handled/processed/received
 *  - trait PerformanceRecording
 *  - Support for some (simple) Metrics (run time, accuracy => maybe as an example for recording *custom* metrics! that would be quite nice :)
 *  - Support for configuring performance data sink
 *
 *  v0.6: Can we integrate other simulation software to some extent?
 *  - Integrate BioPEPA: at least parameter scan + instrumentation should work...
 *  - Set up cross-validation experiment with SSAs in James II
 *  - Support for SED-ML in the form of an export function, i.e. toSEDML(exp1) (just to pinpoint the differences, and to show that we can do this :)
 *
 *  v0.7: Additional features for ALeSiA
 *  - Support for hypothesis testing
 *  - Support for (partial) 'experiment plans' => directed graphs of experiments and hypothesis checks...
 *  - Start building up a 'library' of custom performance experiments
 *  - Clean up, refactoring, manual
 *
 *  ==> Then move to J2 repo / bitbucket - let's see...
 *
 *  @author Roland Ewald
 */
@Test class TestJamesExperiments {

  /** Tests parameter scanning experiment with some extras. */
  @Test def testParameterScan() = {

    val expectedSetups = 20
    val fixedValue = 345

    import sessl._
    import sessl.james._

    var repDoneCounter = 0
    var expDoneCounter = 0

    TestCounter.reset()
    val exp = new Experiment(testCounterModel) with Instrumentation with ParallelExecution {

      stopTime = 1.0
      replications = 2

      rng = MersenneTwister(1234)

      define("fixedVar" ==> fixedValue, "answer" ==> "no!")

      scan("upperVar" ==> (1, 2), { "testDouble" ==> range(1., 1., 10.) } and { "testInt" ==> range(21, 1, 30) } + { "testLong" ==> range(31L, 1L, 40L) })

      observeAtTimes(.1, .2, .3, .9) { // currently no true 'scope', but would be possible...
        bind("x" to "S1", "y" ~ "S1")
      }

      withRunResult { //specifies what shall be done after each run
        results =>
          {
            println("Last y-value:" + results("y"))
          }
      }

      withReplicationsResult { //specifies what shall be done after all runs for one variable assignment have been completed
        results =>
          {
            repDoneCounter += 1
            TestCounter.checkEquality("There should be exactly the number of desired replications (" + replications +
              ") in the result set, but there are: " + results("y").size, replications, results("y").size)
          }
      }

      withExperimentResult { //specifies what shall be done after experiment execution
        results =>
          {
            expDoneCounter += 1
            println("y-results:" + results("y"))
            println("Variance of y-results:" + results.variance("y"))
            TestCounter.checkEquality("The size without restrictions should always be the same.", replications * expectedSetups, results("y").size)
            TestCounter.checkEquality("The given restriction should not restrict anything.", replications * expectedSetups, results.having("fixedVar" ==> fixedValue)("y").size)
            TestCounter.checkEquality("Exactly one configuration should have upperVar=1, testInt=25:", replications, results.having("upperVar" ==> 1, "testInt" ==> 25)("y").size)
          }
      }

      // Note that system-specific configuration is ALWAYS possible, too! 
      exp.getFixedModelParameters.put("theAnswer", new java.lang.Integer(42))

      parallelThreads = -1 //Leave one core idle... 
      // Simple configuration tasks are done by case classes...
      simulator = DirectMethod
    }

    execute(exp)

    //Checking results
    TestCounter.checkValidity(exp.replications * expectedSetups, paramCounterMap => paramCounterMap.size == expectedSetups && paramCounterMap.forall(mapEntry => mapEntry._2 == exp.replications))
    assertEquals("ReplicationsDone should be called as often as there are setups.", expectedSetups, repDoneCounter)
    assertEquals("Only one experiment has been finished.", 1, expDoneCounter)
  }

  /** Tests whether optimization works. */
  @Test def testOptimization() = {

    import sessl._
    import sessl.james._

    val exp = new Experiment(testModel) with Instrumentation with Optimization {

      stopTime = 20.5
      replications = 2
      rng = MersenneTwister()

      optimizeFor("x" ~ "S1", "y" ~ "S0")(results => { results.mean("y") + results.mean("x") }) {
        optimizeOnAllConfigs = false //<- can be left away, the default is false
        optimizer = HillClimbing
        optimize("#species" ~ "numSpecs", range(10, 100))
        startOptimizationWith("#species" ==> 12)
        optStopPolicy = OptMaxTime(seconds = 50) or OptMaxAssignments(2) //<- careful, and/or operators have the same priority, use parentheses!
      }
    }
    execute(exp)
  }

  /** Tests whether setting up the RNG works. */
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

    val exp1 = new Experiment(testModel) {
      stopTime = .01
      rng = LCG(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.lcg.LCG].getName))
    }
    val exp2 = new Experiment(testModel) {
      stopTime = .01
      rng = MersenneTwister(testSeed)
      afterExperiment(testFunction(classOf[_root_.james.core.math.random.generators.mersennetwister.MersenneTwister].getName))
    }
    execute(exp1, exp2)
  }

  /** Tests whether writing to the database works. */
  @Test def testDBDataSinkExperiment() = {
    import sessl._
    import sessl.james._

    val exp = new Experiment(testModel) with Instrumentation with DataSink {

      stopTime = 0.1
      dataSink = MySQLDataSink(schema = "test")
    }
    execute(exp)
  }

  @Test def testBriefingsExperiment() = {

    import sessl._
    import sessl.james._

    val exp = new Experiment("file-sr:/./SimpleModel.sr") with Instrumentation with ParallelExecution with DataSink with Optimization {

      //Basic setup
      stopTime = 100000

      //Experiment design
      optimizeFor("x" ~ "A")(results => results.max("x")) { //<- diff: uses curve fitting with some real data, could be easily provided like this: curveFitting(results.trajectory("x"), realData)
        optStopPolicy = OptMaxAssignments(10) or OptMaxTime(hours = 1) //diff: was 100, but this takes too long for testing, no time constraint! ;-)
        optimize("synthRate" ~ "r1", range(1.0, 10.0))
        optimize("degradRate" ~ "r2", range(5.0, 15.0))
        startOptimizationWith("synthRate" ==> 1.0, "degradRate" ==> 5.0)
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
    execute(exp)
  }

  @Test def testReporting() = {

    import sessl._
    import sessl.james._

    val exp = new Experiment(testModel) with Instrumentation with Report with ParallelExecution {

      stopTime = 0.5
      replications = 10
      observePeriodically(range(0.0, 0.05, 0.5)) { bind("x" ~ "S0", "y" ~ "S1") }

      //Some reporting: 
      reportName = "My SESSL Test Report"
      reportDescription = "This was generated by the James II 'Report' trait in Sessl."

      withRunResult {
        results =>
          {
            reportSection("From run " + results.id) {
              linePlot(results ~ ("x"), results ~ ("y"))(title = "The trajectories of x and y!")
            }
          }
      }

      withExperimentResult {
        results =>
          {
            reportSection("My test section") {
              reportSection("Test-Section sessl-A") {
                scatterPlot(results ~ ("x"), results ~ ("y"))(title = "Test-Title", yLabel = "Overridden sessl-label for y-axis", caption = "This is a sessl figure.")
              }
              reportSection("Test-Section sessl-B") {
                histogram(results ~ ("x"))(title = "A fancy histogram.")
                boxPlot(results ~ ("x"), results ~ ("y"))(title = "A boxplot (with named variables)")
                boxPlot(results("x"), results("y"))(title = "Another boxplot (without names)")
                reportStatisticalTest(results ~ ("x"), results ~ ("y"))()
                reportTable(results ~ ("x"), results ~ ("y"))(caption = "This is a table")
              }
            }
          }
      }
    }

    //Best-effort deletion, don't check result (some open programs like R may prevent the deletion of ALL files...)
    MiscUtils.deleteRecursively(exp.reportName)
    val rawDataDir = new File(exp.reportName + "/raw")
    assertFalse("Directory containing raw data should have been deleted.", rawDataDir.exists)

    execute(exp)

    assertTrue("After execution, a directory for raw data should exist.", rawDataDir.exists)
    val files = new File(exp.reportName).list.toSet
    assertTrue("The directory for raw data, the auxiliary plotting methods, and the report itself should be there", files("raw") && files("plotting.R") && files("report.Rtex"))
    val dataFiles = rawDataDir.listFiles
    assertTrue("There should be some files containing the raw data, and none of them should be empty.", dataFiles.length > 0 && dataFiles.forall(_.length > 0))
  }

  @Test def testStoppingCriteria() = {

    import sessl._
    import sessl.james._

    //How to re-use experiment definitions: as normal classes...
    class DefaultExperiment extends Experiment(testModel) with ParallelExecution {
      replications = 2
    }

    execute(new DefaultExperiment() {
      stopCondition = Never or (AfterSimTime(0.5) and AfterSimSteps(1000)) //Never say 'Never() and ...' :)      
    })

    val exp1Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimSteps(0) })
    val exp2Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimSteps(10000) })
    val exp3Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimTime(0.5) })
    val exp4Time = measureExecTime(new DefaultExperiment() { stopCondition = AfterSimTime(0.6) and AfterWallClockTime(seconds = 3) })

    assertTrue("Not simulating at all should always be the fastest.", exp1Time < exp2Time && exp1Time < exp3Time && exp1Time < exp4Time)
    assertTrue("", exp3Time < exp4Time)
  }

  /** Measures execution time of an experiment. */
  def measureExecTime(exp: AbstractExperiment) = {
    val sw = new StopWatch()
    sw.start();
    AbstractExperiment.execute(exp);
    sw.stop();
    sw.elapsedMilliseconds()
  }

  @Test def testReplicationCriteria() = {

    import sessl._
    import sessl.james._

    val manyReps = 20

    var numberOfReps = -1

    // Combine replication conditions via 'and'
    val expAnd = new Experiment(testModel) with Instrumentation with ParallelExecution {
      stopTime = 1.5
      observeAtTimes(0.45) { bind("x" ~ "S3") }
      //TODO: Jan wg. CI-replication criteria und dataID/attrib fragen...
      //replicationCondition = FixedNumber(1) and (FixedNumber(manyReps) or MeanConfidenceReached("x"))
      replicationCondition = FixedNumber(1) and FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expAnd)
    assertEquals("The number of replications should match", manyReps, numberOfReps)

    // Combine replication conditions via 'or'
    val expOr = new Experiment(testModel) with Instrumentation with ParallelExecution {
      stopTime = 0.5
      observeAtTimes(0.45) { bind("x" ~ "S3") }
      replicationCondition = FixedNumber(1) or FixedNumber(manyReps)
      withExperimentResult { r => numberOfReps = r("x").length }
    }
    execute(expOr)
    assertEquals("The number of replications should match", 1, numberOfReps)
  }
}

object TestJamesExperiments {
  /** Model that fills the test counter object. */
  val testCounterModel = "java://tests.sessl.james.BogusLCSModel"

  /** Default test model. */
  val testModel = "java://examples.sr.LinearChainSystem"
}