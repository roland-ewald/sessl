package tests.sessl.james

import org.junit.Assert._
import org.junit.Test

import sessl.util.CreatableFromVariables

/** Some tests for performance experiments.
 *  @author Roland Ewald
 */
@Test class TestPerformanceExperiments {

  case class TestAlgo(x: Double = 0.5, y: String = "default") extends CreatableFromVariables[TestAlgo]
  case class TestAlgo2(x1: Int = 1, x2: Int = 2, x3: Int = 3, x4: Int = 4) extends CreatableFromVariables[TestAlgo2]
  case class TestAlgo3(x1: Int = 0, subAlgo: TestAlgo2 = TestAlgo2(), x45: Int = 17) extends CreatableFromVariables[TestAlgo3]

  /** Tests construction of simulator sets. */
  @Test def testSimulatorSets() = {

    import sessl._

    //Testing corner case and simple cases
    assertEquals(1, (TestAlgo2() scan ()).size)
    assertEquals(3, (TestAlgo() scan ("x" <~ (1, 2, 3), "y" <~ "test")).size)
    assertEquals(10, (TestAlgo() scan ("x" <~ range(.1, .1, 1), "y" <~ "test")).size)
    assertEquals(10, (TestAlgo2() scan ("x1" <~ range(1, 1, 10) and "x2" <~ range(1, 1, 10))).size)

    //Testing more complex case
    val fixedX3 = 1223
    val setups = TestAlgo2(x3 = fixedX3) scan ("x1" <~ range(1, 1, 10) and "x2" <~ range(1, 1, 10), "x4" <~ range(20, -1, 1))
    assertEquals(200, setups.size)
    assertEquals(TestAlgo2(1, 1, fixedX3, 20), setups.head)
    assertEquals(TestAlgo2(10, 10, fixedX3, 1), setups.last)

    //Testing nested algorithms
    val fixedX45 = 6
    val nestedSetups = TestAlgo3(x45 = fixedX45) scan (
      "x1" <~ (4, 10, 15),
      "subAlgo" <~ {
        TestAlgo2() scan {
          "x1" <~ range(1, 1, 10)
        }
      })
    assertEquals(30, nestedSetups.size)
    assertEquals(TestAlgo3(4, TestAlgo2(), fixedX45), nestedSetups.head)
    assertEquals(TestAlgo3(15, TestAlgo2(x1 = 10), fixedX45), nestedSetups.last)

    //TODO: provide helper objects like EventQueues(ignore="...") = Seq[defaults...] of all EQs available, etc.
  }

  @Test def testExperimentOnSimulatorSetAdaptive() = {

    import sessl._
    import sessl.james._

    //TODO: Make this independent of instrumentation mix-in!
    var counter = 0
    val tauLeapingAlgorithms = TauLeaping() scan ("epsilon" <~ range(0.02, 0.01, 0.05))
    val nrAlgorithms = NextReactionMethod() scan ("eventQueue" <~ Seq(BucketQueue(), LinkedList(), Heap(), SortedList()))
    val exp = new Experiment with ParallelExecution with PerformanceObservation with Report {

      model = TestJamesExperiments.testModel
      stopTime = 1.5
      replications = 200

      afterRun { r => { counter += 1 } }

      simulators <~ (nrAlgorithms ++ tauLeapingAlgorithms)

      simulatorExecutionMode = AnySimulator

      performanceDataSink = FilePerformanceDataSink()

      reportName = "Performance Report"

      withExperimentPerformance { r =>
        reportSection("Results") {
          histogram(r.runtimes)(title = "All run times")
          histogram(("Runtimes of all tau-leaping methods.", r.runtimes(tauLeapingAlgorithms)))(title = "Run times for Tau Leaping")
          histogram(r.runtimes(nrAlgorithms))(title = "Run times for Next Reaction Methods")
          boxPlot(r.runtimesForAll)(title = "Run time per setup")
          boxPlot(r.runtimesFor(tauLeapingAlgorithms))(title = "Run time per setup, for a subset")
          boxPlot(("TL", r.runtimes(tauLeapingAlgorithms)), ("NRM", r.runtimes(nrAlgorithms)))(title = "Run time comparison for algorithm families.")
        }
      }
    }
    execute(exp)
    assertEquals("There should be as many runs as replications were configured.", exp.replications, counter)
  }

  @Test def testExperimentOnSimulatorSet() = {

    import sessl._
    import sessl.james._

    var counter = 0
    val exp = new Experiment with ParallelExecution with PerformanceObservation with Report {

      model = TestJamesExperiments.testModel
      stopTime = 1.5
      replications = 10

      reportName = "Performance Report 2"

      scan("numOfSpecies" <~ (10, 20), "nothing" <~ (1, 2))

      simulators <~ { NextReactionMethod() scan ("eventQueue" <~ (LinkedList(), BucketQueue())) }
      simulators <~ { TauLeaping() scan ("epsilon" <~ range(0.02, 0.005, 0.05)) }

      simulatorExecutionMode = AllSimulators
      parallelThreads = -1

      withRunPerformance { r => println("Runtime:" + r.runtime) }
      withReplicationsPerformance { r =>
        reportSection("Results for assignment " + r.results.id) {
          scatterPlot(r.runtimesFor(NextReactionMethod(eventQueue = LinkedList())),
            r.runtimesFor(TauLeaping(epsilon = 0.025)))(title = "A scatterplot for a single assignment.")
        }
      }

      withExperimentPerformance { r =>
        reportSection("Experiment-Wide Results") {
          scatterPlot(r.runtimesFor(NextReactionMethod(eventQueue = LinkedList())),
            r.runtimesFor(TauLeaping(epsilon = 0.025)))(title = "Showing data for the whole experiment.")
          histogram(r ~ "runtime")(title = "All runtimes.")
          histogram(r.having("numOfSpecies" <~ 10) ~ "runtime")(title = "All runtimes for a sub-set of assignments.")
        }
      }

      afterRun { r => println(r.aspectFor(classOf[AbstractInstrumentation])); counter += 1 }
    }
    execute(exp)
    assertEquals(exp.simulators.size * exp.replications * 4, counter)
  }

}