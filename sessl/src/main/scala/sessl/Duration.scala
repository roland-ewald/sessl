/*******************************************************************************
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
 ******************************************************************************/
package sessl

/**
 * Represents a duration and provides conversion methods.
 *  @author Roland Ewald
 */
trait AbstractDuration {

  //Factors for time conversions
  private val toMS = 1000
  private val toS = 60
  private val toM = 60
  private val toH = 24

  /** Unit-less time. Will be ignored for the calculations, unless it is non-zero. */
  def time: Double = 0.

  /** The number of days. */
  def days: Int

  /** The number of hours (on top of full days). */
  def hours: Int

  /** The number of minutes (on top of full hours). */
  def minutes: Int

  /** The number of seconds (on top of full minutes). */
  def seconds: Int

  /**
   * Get the amount of milliseconds.
   *  @return the given duration in milliseconds
   */
  def toMilliSeconds: Long = {
    (days * toH * toM * toS * toMS) + (hours * toM * toS * toMS) + (minutes * toS * toMS) + (seconds * toMS)
  }

  def toSeconds: Double = toMilliSeconds / toMS

  def toMinutes: Double = toSeconds / toS

  def toHours: Double = toMinutes / toM

  def toDays: Double = toHours / toH

  /** Get the amount of milli-seconds or the unit-less time (if > 0). */
  def asMilliSecondsOrUnitless = if (time > 0) time.toLong else toMilliSeconds

  /** Get the amount of seconds or the unit-less time (if > 0). */
  def asSecondsOrUnitless: Double = if (time > 0) time else toSeconds
}

/** Default duration (zero). */
case class Duration(override val time: Double = 0, days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) extends AbstractDuration

