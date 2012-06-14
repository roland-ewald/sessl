package sessl
import sessl.util.Duration

/** Support for configuring the conditions at which a simulation run should stop.
 *  @author Roland Ewald
 */
trait SupportStoppingConditions {

  /** Stores the fixed simulation end time (if one is set). */
  protected[sessl] var fixedStopTime: Option[Double] = None

  /** Stores a more complex stopping condition (if one is set). */
  protected[sessl] var stoppingCondition: Option[StoppingCondition] = None

  /** Getting/setting the stop time. */
  def stopTime_=(time: Double) = { fixedStopTime = Some(time) }
  def stopTime: Double = fixedStopTime.get

  /** Getting/setting a complex stopping condition. */
  def stopCondition_=(sc: StoppingCondition) = {
    require(!stoppingCondition.isDefined,
      "Stopping condition has already been defined as '" + stoppingCondition.get + "', found conflicting definition as '" + sc + "'")
    stoppingCondition = Some(sc)
  }
  def stopCondition: StoppingCondition = stoppingCondition.get

  /** Check stopping setup and get stopping condition.
   *  @return the stopping condition to be used
   */
  protected[sessl] def checkAndGetStoppingCondition() = {
    require(fixedStopTime.isDefined || stoppingCondition.isDefined,
      "No stopping condition is specified (use, e.g., stopTime= 1.0 or stopCondition=...).")
    require(!(fixedStopTime.isDefined && stoppingCondition.isDefined),
      "Both a fixed stop time (" + fixedStopTime.get + ") and a stopping condition (" +
        stoppingCondition.get + ") are set - only one is allowed. Use '" + AfterSimTime(fixedStopTime.get) +
        "' to add the fixed stop time condition to the conditions.")
    if (fixedStopTime.isDefined)
      AfterSimTime(fixedStopTime.get) else stoppingCondition.get
  }
}

//Class hierarchy for stopping conditions

/** Super type of all stopping conditions. */
trait StoppingCondition

/** Never really stop the simulation run (it has to stop on its own).*/
case object Never extends StoppingCondition

/** Stop after a given amount of simulation time. */
case class AfterSimTime(override val time: Double = 0., days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) extends StoppingCondition with Duration

/** Stop after a given amount of wall-clock time. */
case class AfterWallClockTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) extends StoppingCondition with Duration

case class AfterCPUTime()

/** Stop after a given number of simulation steps.
 *  @param steps the number of steps after which the simulation can be stopped
 */
case class AfterSimSteps(steps: Long) extends StoppingCondition

/** Stop if both given conditions are fulfilled. */
case class ConjunctiveStoppingCondition(left: StoppingCondition, right: StoppingCondition) extends StoppingCondition

/** Stop if any of the given conditions is fulfilled. */
case class DisjunctiveStoppingCondition(left: StoppingCondition, right: StoppingCondition) extends StoppingCondition

/** Combines two stopping conditions (either with OR or with AND).
 *  @param left the first stopping condition
 */
case class CombinedStoppingCondition(left: StoppingCondition) extends StoppingCondition {
  def or(right: StoppingCondition) = new DisjunctiveStoppingCondition(left, right)
  def and(right: StoppingCondition) = new ConjunctiveStoppingCondition(left, right)
}
