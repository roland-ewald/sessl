package sessl

/** Support to configure a random-number generator.
 *  @author Roland Ewald
 */
trait SupportRNGSetup {

  /** The random number generator to be used (if set).*/
  protected[sessl] var randomNumberGenerator: Option[RNG] = None

  /** Getting/setting the RNG. */
  def rng_=(rand: RNG) = { randomNumberGenerator = Some(rand) }
  def rng: RNG = { randomNumberGenerator.get }

}