package sessl.sbw

import scala.collection.parallel.ForkJoinTaskSupport
import sessl.AbstractParallelExecution

trait ParallelExecution extends AbstractParallelExecution {
  this: Experiment =>

  /** The number of threads to be used. */
  private[this] var numberOfThreads = 1
  
  private var jobCounter: Map[ConfigDescription, Int] = null

  override def configureParallelExecution(threads: Int) = {
    numberOfThreads = threads
  }
  
  

  override def executeConfigs(configs: List[ConfigDescription]) = {   
    jobCounter = configs.zip(List(0)).toMap
    val jobs = configs.zipWithIndex
    val parallelJobs = jobs.par
    parallelJobs.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(numberOfThreads))
    jobs.par.map(executeJob).toList
  }
  
  override def finishRun(jobDesc: JobDescription) {
    val simulatorDesc = jobDesc._1._2
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    //Register run execution
    var counter = 0
    this.synchronized {
      counter = jobCounter.get(assignmentDesc, simulatorDesc).get.+(1)
      jobCounter += ((assignmentDesc, simulatorDesc) -> counter)
    }
    runDone(jobDesc._2 + 1);
    //Register replications execution if this is the last setup
    if (counter >= replications) {
      replicationsDone(assignmentDesc._2 + 1)
    }
  }

}