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
package sessl.util

import scala.collection.mutable.ListBuffer
import sessl.Algorithm
import sessl.Simulator

/** Represents a (multi-)set of sessl algorithm representations. For convenience, the order
 *  with which the elements are added is currently preserved.
 *
 *  @param <T> the type of the algorithm contained in the list
 *  @author Roland Ewald
 */
case class AlgorithmSet[T <: Algorithm](initialAlgos: Seq[T]) extends Logging {

  /** The list containing the algorithms. */
  private val algorithmList = {
    val algos = ListBuffer[T]()
    algos ++= initialAlgos
    algos
  }

  /** Empty constructor for convenience. */
  def this() = this(Seq())

  /** Constructor for single-element sets. */
  def this(algorithm: T) = this(Seq(algorithm))

  /** Adding elements to the set. */
  def <~(algos: Seq[T]) = { algorithmList ++= algos }
  def <+(algos: T*) = { algorithmList ++= algos }

  /** Get the defined algorithms. */
  lazy val algorithms: List[T] = algorithmList.toList

  /** Get the set of defined algorithms*/
  lazy val algorithmSet: Set[T] = {
    val set = Set(algorithms: _*)
    if (set.size != algorithms.size)
      logger.warn("Warning: there are duplicated elements in algorithms set:\n" + algorithms.mkString("\n"))
    set
  }

  /** Checks if algorithm set is empty. */
  def isEmpty = algorithmList.isEmpty

  /** Get the size. */
  def size = algorithmList.size

  /** Clear the list. */
  def clear() = algorithmList.clear()

  /** Get the first algorithm. */
  def firstAlgorithm = algorithmList.toList(0)

  /** Checks whether the algorithm set consists only of a single element. */
  def hasSingleElement = algorithmList.size == 1
}

object AlgorithmSet {

  /** Simple static constructor. */
  def apply[T <: Algorithm](): AlgorithmSet[T] = new AlgorithmSet()

  /** Constructor for single-element sets. */
  def apply[T <: Algorithm](algorithm: T) = new AlgorithmSet(algorithm)
}
