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
package sessl.util.test

import scala.collection.mutable.ListBuffer
import sessl.util.Logging

/**
 * Simple counter of executed parameter setups to check correctness of experiment implementations.
 *
 *  @author Roland Ewald
 *
 */
object TestCounter extends Logging {

  /** The type of the more detailed data structure for counting. */
  type ParamCounterMap = scala.collection.mutable.Map[Map[String, Object], Int]

  /** Counts the number of executions. */
  private var execCounter = 0

  private val equalityViolations = ListBuffer[String]()

  private val paramCombinations: ParamCounterMap = scala.collection.mutable.Map[Map[String, Object], Int]()

  def registerParamCombination(params: java.util.Map[java.lang.String, java.lang.Object]) = {
    
    synchronized {
      execCounter += 1
      val scalaMap = scala.collection.JavaConversions.mapAsScalaMap(params).toMap
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
    logger.info("#Executions:" + execCounter)
    logger.info("Parameter-Counts:" + paramCombinations.mkString("\n", "\n", "\n"))
  }
}
