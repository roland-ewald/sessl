package sessl.util

/** Represents a duration and provides conversion methods.
 *  @author Roland Ewald
 */
trait Duration {

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

  /** Get the amount of milliseconds.
   *  @return the given duration in milliseconds
   */
  def toMilliSeconds: Long = {
    val toMS = 1000
    val toS = 60
    val toM = 60
    val toH = 24
    (days * toH * toM * toS * toMS) + (hours * toM * toS * toMS) + (minutes * toS * toMS) + (seconds * toMS)
  }

  /** Get the amount of seconds or the unit-less time (if > 0). */
  def asMilliSecondsOrUnitless = if (time > 0) time.toLong else toMilliSeconds
}