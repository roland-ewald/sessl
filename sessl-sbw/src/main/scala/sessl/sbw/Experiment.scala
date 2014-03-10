package sessl.sbw

import sessl.AbstractExperiment
import edu.caltech.sbw._
import sessl.sbw.util._
import scala.io.Source

/**
 * Stub for SBW integration. 
 * 
 * @author Roland Ewald
 */
class Experiment extends AbstractExperiment {
  
  private var sbml:String = null
  
  /** Describes a variable assignment (first element) and its id (second element). */
  type AssignmentDescription = (Map[String, Any], Int)
  
  /** Describes a job with a an id as second element and a triple (variable assignment, simulator-setup,flag-replications-done) as a first element. */
  type JobDescription = ((AssignmentDescription, BasicSBWSimulator, Boolean), Int)
  
  /**
   * Setup
   */
  override def basicConfiguration() = {
    SBW.connect()
    sbml = Source.fromFile(modelLocation.get).mkString
    /* Configure simulator setup. */
    require(fixedStopTime.isDefined, "No stop time is given. Use stopTime =... to set it.")
    if (simulators.isEmpty)
      simulators <+ Gillespie()
    simulators.algorithms.foreach(s => require(s.isInstanceOf[BasicSBWSimulator], "Simulator '" + s + "' is not supported."))
  }
 
  /**
   * Execution
   */
  override def executeExperiment() = {
    
    val jobs = for (v <- createVariableSetups().zipWithIndex; i <- simulators.algorithms.indices) yield (v, simulators.algorithms(i).asInstanceOf[BasicSBWSimulator], i == simulators.size - 1)
    require(!jobs.isEmpty, "Current setup does not define any jobs to be executed.")

    //Execute all generated jobs
    executeJobs(jobs.zipWithIndex)
    
    experimentDone()
      
//      for (method:ServiceMethod <- service.getMethods()) {
//        println(method.getSignatureString())
//      }
    
  }
  
  /** Executes the given list of jobs. */
  def executeJobs(jobs: List[JobDescription]) = jobs.map(executeJob)

  /** Executes a job. */
  protected[sbw] final def executeJob(jobDesc: JobDescription) = {
    var engine = jobDesc._1._2.create()
    val assignmentDesc: AssignmentDescription = jobDesc._1._1
    val runId = jobDesc._2 + 1
    val variableAssignment = assignmentDesc._1 ++ fixedVariables
    val assignmentId = assignmentDesc._2 + 1
    try  {
      println("***Load the model***")
      engine.loadSBML(sbml)
      println("***Set Parameters***")
      for (variable <- variableAssignment) {
        engine.setParameter(variable._1, variable._2.asInstanceOf[Double])
      }
      var results = engine.simulate(0.0, stopTime, 1)
      println("***Executed Simulation***")
    } catch {
      case e : SBWException => e.printStackTrace()
    } finally {
      jobDesc._1._2.shutDownModule
      //Register run execution
      addAssignmentForRun(runId, assignmentId, assignmentDesc._1.toList)
      runDone(runId);
      //Register replications execution if this is the last setup
      if (jobDesc._1._3) {
        replicationsDone(assignmentId)
      }
    }
  }
  
  /**
   * Finalize
   */
  override def finishExperiment() = {
    SBW.disconnect()
  }
}