package sessl

import scala.collection.mutable.Map

import sessl.util.ResultOperations

/** Super type of all result aspects. A result aspect additional result data
 *  that can be associated with a certain kind of result.
 *  @param R the type of result this aspect can be associate with
 */
trait ResultAspect[R <: Result] {

  /** Reference to the concrete result it is associated with. */
  private[this] var result: Option[R] = None

  /** The owner of this result aspect. Necessary to easily filter a set of results to be useful for a certain owner. */
  def owner: Result.Owner

  /** Provides access to the general results object. Note that you may use this reference to get *another* kind of result aspect. */
  def results = result.getOrElse { throw new IllegalArgumentException("The result aspect '" + this + "' is not configured correctly (no reference to result is set).") }

  /** Sets the result aspect, should be done by the result aspect management.
   *  @see sessl.ResultAspectManagement
   */
  private[sessl] def setResult(r: R) = {
    result = Some(r)
  }
}

/** Super class for all run results aspects. */
abstract class RunResultsAspect(myOwner: Result.Owner) extends ResultOperations with ResultAspect[RunResults] {
  /** The aspect id is the id of its run. */
  def id = results.id

  /** Return the owner. */
  override def owner = myOwner
}

/** Super class for all replication results aspects. */
abstract class ReplicationsResultsAspect(myOwner: Result.Owner) extends ResultOperations with ResultAspect[ReplicationsResults] {

  /** Stores all run result aspects of the same owner, to ease access when working with the results. */
  val runsResults = Map[Int, RunResultsAspect]()

  /** Will only be called once, by {@link sessl.ReplicationsResults}.*/
  private[sessl] def setResults(results: Map[Int, RunResultsAspect]) {
    Result.cleanAdd(runsResults, results)
  }

  /** Return the owner. */
  override def owner = myOwner
}

/** Super class for all experiment results aspects. */
abstract class ExperimentResultsAspect(myOwner: Result.Owner) extends ResultOperations with ResultAspect[ExperimentResults] {

  /** Stores all run result aspects of the same owner, to ease access when working with the results. */
  val runsResults = Map[Int, RunResultsAspect]()

  /** Stores all replication results aspects of the same owner, to ease access when working with the results. */
  val replicationsResults = Map[Int, ReplicationsResultsAspect]()

  /** Return the owner. */
  override def owner = myOwner

  /** Will only be called once, by {@link ExperimentResults}.*/
  private[sessl] def setResults(rResults: Map[Int, RunResultsAspect], repResults: Map[Int, ReplicationsResultsAspect]) {
    Result.cleanAdd(runsResults, rResults)
    Result.cleanAdd(replicationsResults, repResults)
  }

}
