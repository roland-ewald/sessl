package sessl.util

/**
 * Represents a duration and provides conversion methods.
 * @author Roland Ewald
 */
trait Duration {

  def days: Int
  
  def hours: Int

  def minutes: Int

  def seconds: Int

  /** Get the amount of milliseconds.
   * @return the given duration in milliseconds 
   */
  def toMilliSeconds: Long = {
    val toMS = 1000
    val toS = 60
    val toM = 60
    val toH = 24
    (days * toH * toM * toS * toMS) + (hours * toM * toS * toMS) + (minutes * toS * toMS) + (seconds * toMS)
  }
  
}