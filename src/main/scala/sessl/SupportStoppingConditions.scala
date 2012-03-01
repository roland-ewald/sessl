package sessl
import sessl.util.Duration

/** Support for configuring the conditions at which a simulation run should stop.
 *  @author Roland Ewald
 */
trait SupportStoppingConditions {

  /** Stores the fixed simulation end time (if one is set). */
  protected[sessl] var fixedStopTime: Option[Double] = None

  /** Stores a more complex stopping criterion (if one is set). */
  protected[sessl] var stoppingCriterion: Option[StoppingCriterion] = None

  /** Getting/setting the stop time. */
  def stopTime_=(time: Double) = { fixedStopTime = Some(time) }
  def stopTime: Double = fixedStopTime.get

  /** Getting/setting a complex stopping condition. */
  def stopCondition_=(sc: StoppingCriterion) = {
    require(!stoppingCriterion.isDefined,
      "Stopping criterion has already been defined as '" + stoppingCriterion.get + "', found conflicting definition as '" + sc + "'")
    stoppingCriterion = Some(sc)
  }
  def stopCondition: StoppingCriterion = stoppingCriterion.get

  /** Check stopping setup and get stopping criterion.
   *  @return the stopping criterion to be used
   */
  protected[sessl] def checkAndGetStoppingCriterion() = {
    require(fixedStopTime.isDefined || stoppingCriterion.isDefined, "No stopping condition is specified (use, e.g., stopTime= 1.0 or stopCondition=...).")
    require(!(fixedStopTime.isDefined && stoppingCriterion.isDefined), "Both a fixed stop time (" + fixedStopTime.get + ") and a stopping condition (" +
      stoppingCriterion.get + ") are set - only one is allowed. Use '" + AfterSimTime(fixedStopTime.get) + "' to add the fixed stop time criterion to the condition.")
    if (fixedStopTime.isDefined)
      AfterSimTime(fixedStopTime.get) else stoppingCriterion.get
  }
}

//Class hierarchy for stopping criteria

/** Super type of all stopping criteria. */
trait StoppingCriterion

/** Never really stop the simulation run (it has to stop on its own).*/
case object Never extends StoppingCriterion

/** Stop after a given amount of simulation time.
 *  @param time the simulation time after which the simulation can be stopped
 */
case class AfterSimTime(time: Double) extends StoppingCriterion

/** Stop after a given amount of wall-clock time.
 *  @param time the wall-clock time after which the simulation can be stopped
 */
case class AfterWallClockTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) extends StoppingCriterion with Duration

/** Stop after a given number of simulation steps.
 *  @param steps the number of steps after which the simulation can be stopped
 */
case class AfterSimSteps(steps: Long) extends StoppingCriterion

/** Stop if both given criteria are fulfilled. */
case class ConjunctiveStoppingCriterion(left: StoppingCriterion, right: StoppingCriterion) extends StoppingCriterion

/** Stop if any of the given criteria is fulfilled. */
case class DisjunctiveStoppingCriterion(left: StoppingCriterion, right: StoppingCriterion) extends StoppingCriterion

/** Combines two stopping criteria (either with OR or with AND).
 *  @param left the first stopping criterion
 */
case class CombinedStoppingCriterion(left: StoppingCriterion) extends StoppingCriterion {
  def or(right: StoppingCriterion) = new DisjunctiveStoppingCriterion(left, right)
  def and(right: StoppingCriterion) = new ConjunctiveStoppingCriterion(left, right)
}
