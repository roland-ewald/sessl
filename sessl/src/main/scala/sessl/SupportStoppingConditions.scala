/**
 * *****************************************************************************
 * Copyright 2012 Roland Ewald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package sessl

/**
 * Support for configuring the conditions at which a simulation run should stop.
 *  @author Roland Ewald
 */
trait SupportStoppingConditions {

  /** Stores the fixed simulation end time (if one is set). */
  protected[sessl] var fixedStopTime: Option[Double] = None

  /** Stores a more complex stopping condition (if one is set). */
  protected[sessl] var stoppingCondition: Option[StoppingCondition] = None

  /** Specify a fixed stop time. */
  def stopTime_=(time: Double) = { fixedStopTime = Some(time) }
  
  /** Get the fixed stop time. */
  def stopTime: Double = fixedStopTime.get

  /** Specify stopping condition. See sub-classes of [[StoppingCondition]]. */
  def stopCondition_=(sc: StoppingCondition) = {
    require(!stoppingCondition.isDefined,
      "Stopping condition has already been defined as '" + stoppingCondition.get + "', found conflicting definition as '" + sc + "'")
    stoppingCondition = Some(sc)
  }
  
  /** Get the specified [[StoppingCondition]]. */
  def stopCondition: StoppingCondition = stoppingCondition.get

  /**
   * Check stopping setup and get stopping condition.
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

/**
 * Stop after a given amount of simulation time.
 *  @param time unit-less time, for custom time units (default is 0)
 *  @param days (default is 0)
 *  @param hours (default is 0)
 *  @param minutes (default is 0)
 *  @param seconds (default is 0)
 *  @param milliseconds (default is 0)
 */
case class AfterSimTime(override val time: Double = .0, days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0) extends StoppingCondition with AbstractDuration

/**
 * Stop after a given amount of wall-clock time.
 *  @param days (default is 0)
 *  @param hours (default is 0)
 *  @param minutes (default is 0)
 *  @param seconds (default is 0)
 *  @param milliseconds (default is 0)
 */
case class AfterWallClockTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0) extends StoppingCondition with AbstractDuration

/**
 * Stop after a given amount of CPU time.
 *  @param days (default is 0)
 *  @param hours (default is 0)
 *  @param minutes (default is 0)
 *  @param seconds (default is 0)
 *  @param milliseconds (default is 0)
 */
case class AfterCPUTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0) extends StoppingCondition with AbstractDuration

/**
 * Stop after a given number of simulation steps.
 *  @param steps the number of steps after which the simulation can be stopped
 */
case class AfterSimSteps(steps: Long) extends StoppingCondition

/**
 * Stop if both given conditions are fulfilled.
 *  @param left the left stopping condition
 *  @param right the right stopping condition
 */
case class ConjunctiveStoppingCondition(left: StoppingCondition, right: StoppingCondition) extends StoppingCondition

/**
 * Stop if any of the given conditions is fulfilled.
 *  @param left the left stopping condition
 *  @param right the right stopping condition
 */
case class DisjunctiveStoppingCondition(left: StoppingCondition, right: StoppingCondition) extends StoppingCondition

/**
 * Combines two stopping conditions (either with OR or with AND).
 *  @param left the first stopping condition
 */
case class CombinedStoppingCondition(left: StoppingCondition) extends StoppingCondition {
  def or(right: StoppingCondition) = new DisjunctiveStoppingCondition(left, right)
  def and(right: StoppingCondition) = new ConjunctiveStoppingCondition(left, right)
}
