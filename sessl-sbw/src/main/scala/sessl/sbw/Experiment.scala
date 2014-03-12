package sessl.sbw

import scala.collection.mutable.SynchronizedMap
import scala.io.Source
import edu.caltech.sbw._
import sessl.AbstractExperiment
import sessl.sbw.algorithms.BiospiceSimDescription
import sessl.sbw.algorithms.RoadRunnerSimDescription
import sessl.sbw.algorithms.JarnacSimDescription
import sessl.sbw.algorithms.BiospiceSimDescription

/**
 * Stub for SBW integration. 
 * 
 * @author Stefan Leye
 */
class Experiment extends AbstractExperiment with SBWResultHandling {
  
  /** Describes a variable assignment (first element) and its id (second element). */
  type AssignmentDescription = (Map[String, Any], Int)
  
  type ConfigDescription = (AssignmentDescription, SBWSimulatorDescription)
  
  /** Describes a job with an id as second element and a triple (variable assignment, simulator-setup,flag-replications-done) as a first element. */
  type JobDescription = (ConfigDescription, Int)
  
  
  private var sbml:String = null
  
  private var jobCounter: Integer = 0
  
  /**
   * Setup
   */
  override def basicConfiguration() = {
    SBW.connect()
    sbml = Source.fromFile(modelLocation.get).mkString
    /* Configure simulator setup. */
    require(fixedStopTime.isDefined, "No stop time is given. Use stopTime =... to set it.")
    if (simulators.isEmpty)
//      simulators <+ new JarnacSimDescription(true)
      simulators <+ new BiospiceSimDescription
    simulators.algorithms.foreach(s => require(s.isInstanceOf[SBWSimulatorDescription], "Simulator '" + s + "' is not supported."))
  }
 
  /**
   * Execution
   */
  override def executeExperiment() = {
    
    val configs = for (v <- createVariableSetups().zipWithIndex; i <- simulators.algorithms.indices; j <- 1 to replications) 
      yield (v, simulators.algorithms(i).asInstanceOf[SBWSimulatorDescription])
    require(!configs.isEmpty, "Current setup does not define any jobs to be executed.")

    //Execute all generated jobs
    executeConfigs(configs)
    
    experimentDone()
//    for (x <- SBW.getModuleDescriptors(true)) {
//      println(x.getDisplayName())
//    }
//      for (method:ServiceMethod <- service.getMethods()) {
//        println(method.getSignatureString())
//      }
    
  }
  
  /** Executes the given list of jobs. */
  def executeConfigs(jobs: List[ConfigDescription]) = jobs.zipWithIndex.map(executeJob)

  /** Executes a job. */
  protected[sbw] final def executeJob(jobDesc: JobDescription) = {
    val simulatorDesc = jobDesc._1._2
    var engine = simulatorDesc.create
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    val runId = jobDesc._2 + 1
    val variableAssignment = assignmentDesc._1 ++ fixedVariables
    val assignmentId = assignmentDesc._2 + 1
    addAssignmentForRun(runId, assignmentId, assignmentDesc._1.toList)
    try  {
      logger.info("Run ID: " + runId + ": load Model")
      engine.loadModel(sbml)
//      println()
//      engine.getNamesOfParameters().map{x => print(x + ", ")}
//      println()
//      engine.getNamesOfVariables().map{x => print(x + ", ")}
//      println()
      val species = engine.getVariableNames
      for (variable <- variableAssignment) {
        engine.setParameter(variable._1, variable._2.asInstanceOf[Double])
      }
      logger.info("Run ID: " + runId + ": execute simulation")
      val results = engine.simulate(stopTime)
//      println(results.length)
      considerResults(runId, assignmentId, species, results)
    } catch {
      case e : SBWException => e.printStackTrace()
    } finally {
      logger.info("Run ID: " + runId + ": finish simulation")
      finishRun(jobDesc)
    }
  }
  
  def finishRun(jobDesc: JobDescription) {
    val simulatorDesc = jobDesc._1._2
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    //Register run execution
    jobCounter = jobCounter + 1
    //Register replications execution if this is the last setup
    runDone(jobDesc._2 + 1);
    if (jobCounter >= replications) {
      jobCounter = 0
      replicationsDone(assignmentDesc._2 + 1)
    }
  }
  
  /**
   * Finalize
   */
  override def finishExperiment() = {
    ModuleHandler.shutDownModules
    SBW.disconnect()
  }
}