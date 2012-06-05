package sessl

import scala.collection.mutable.Map

/** Management of result aspects. This trait should be mixed into all
 *  result types that should support different aspects.
 *  @param R the type of the result this trait is mixed-in with
 *  @param A the type of aspect to be managed
 */
trait ResultAspectManagement[R <: Result, A <: ResultAspect[R]] {
  this: R =>

  /** Contains all aspects, associated with their owner. Each owner may only have *one* aspect. */
  val aspects = Map[Result.Owner, A]()

  /** Add aspect to result. */
  def addAspect(aspect: A) = {
    aspect.setResult(this)
    aspects(aspect.owner) = aspect
  }

  /** Get aspect for a given owner (if exists). */
  def aspectFor(owner: Result.Owner) = aspects.get(owner)

  /** For some map that contains results supporting aspect management as values,
   *  retrieve all result aspects for a given owner.
   *  @param data the results map
   *  @return a map from the original keys to the desired aspect of the associated result
   */
  protected[sessl] def getAspectsFor[X, Y <: Result, Z <: ResultAspect[Y]](data: Map[X, _ <: ResultAspectManagement[Y, Z]], owner: Result.Owner): Map[X, Z] = {
    data.map(e => (e._1, e._2.aspectFor(owner).get))
  }
}
