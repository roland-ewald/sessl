import sessl._
import sessl.james._
import sessl.james.tools.CSVFileWriter

object SampleExperiment extends App {
  
  val modelOutput = CSVFileWriter("./sample_output.csv")

  execute{
    new Experiment with Observation with ParallelExecution {
      replications = 2
      model = "file-sr:/./SampleModel.sr"
      stopTime = 100.0
      observe("P2")
      withRunResult(modelOutput << _.values("P2"))
      observeAt(range(0, 1, 99))
      parallelThreads = -1
    }
  }
}