package tests.sessl

import sessl.util.JavaToScala._
import scala.collection.mutable.ListBuffer

/** Simple counter of executed parameter setups to check correctness of experiment implementations.
 *
 *  @author Roland Ewald
 *
 */
object TestCounter {

  /** The type of the more detailed data structure for counting. */
  type ParamCounterMap = scala.collection.mutable.Map[Map[String, Object], Int]

  /** Counts the number of executions. */
  private var execCounter = 0

  private val equalityViolations = ListBuffer[String]()

  private val paramCombinations: ParamCounterMap = scala.collection.mutable.Map[Map[String, Object], Int]()

  def registerParamCombination(params: java.util.Map[java.lang.String, java.lang.Object]) = {
    synchronized {
      execCounter += 1
      val scalaMap = toScala(params)
      paramCombinations += ((scalaMap, 1 + paramCombinations.get(scalaMap).getOrElse(0)))
    }
  }

  def checkValidity(expectedTotalExecs: Int, checkParamCombis: ParamCounterMap => Boolean) {
    require(execCounter == expectedTotalExecs, "There should be " + expectedTotalExecs + " executions, but " + execCounter + " have been registered.")
    require(checkParamCombis(paramCombinations), "Checking the individual parameter combinations revealed an error.")
    require(equalityViolations.isEmpty, "The following equality violations were encountered:\n\t" + equalityViolations.toList.mkString("\n\t"))
    reset()
  }

  def checkEquality(explanation: String, elem1: Any, elem2: Any) {
    if (elem1 != elem2)
      equalityViolations += explanation
  }

  def reset() = {
    execCounter = 0
    paramCombinations.clear()
    equalityViolations.clear()
  }

  def print() {
    println("#Executions:" + execCounter)
    println("Parameter-Counts:" + paramCombinations.mkString("\n", "\n", "\n"))
  }
}