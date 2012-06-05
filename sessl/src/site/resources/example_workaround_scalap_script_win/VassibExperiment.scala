import sessl._
import sessl.util._
import sessl.tools._
import sessl.james._
	
object VassibExperiment extends App {

    //General experiment: what model, what data
    class AutoRegExperiment extends Experiment with Observation with ParallelExecution {
      model = "file-sr:/./AutoregulatoryGeneticNetwork.sr"
      replications = 1000
      stopTime = 10500
      observe("P2")
      observeAt(range(0, 20, 10000))
      parallelThreads = -1
    }

    val simulators = new AlgorithmSet[Simulator]()
    simulators <~ {
      {
        TauLeaping() scan (
          "epsilon" <~ range(0.01, 0.002, 0.05),
          "gamma" <~ range(5, 1, 15),
          "criticalReactionThreshold" <~ range(0, 5, 45))
      } ++ {
        NextReactionMethod() scan {
          "eventQueue" <~ (Heap(), SortedList())
        }
      }
    }
	
    //Write trajectories and run times for each simulator
    val runtimes = CSVFileWriter("vassib_autoreg_nw_sr_runtimes.csv")
    for (sim <- simulators.algorithms) {
      val modelOutput = CSVFileWriter("vassib_autoreg_nw_sr_trajectories_" + sim + ".csv")
      execute {
        new AutoRegExperiment with PerformanceObservation {
          simulator = sim
          withRunResult(modelOutput << _.values("P2"))
          withReplicationsPerformance { result =>
            runtimes << (sim, result.runtimes)
          }
        }
      }
    }  
}