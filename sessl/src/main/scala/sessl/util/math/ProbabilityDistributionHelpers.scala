package sessl.util.math

/**
 * An interface that provides a collection of helpers for probability distributions.
 *
 * @author Roland Ewald
 */
trait ProbabilityDistributionHelpers {

  /**
   * Calculate quantiles of the chi-square function.
   * Implementations should behave similar to R's qchisq function.
   *
   * @param prob the probability
   * @param prob the degrees of freedom
   * @param tail whether P(X <= x) is calculated (default) or P(X > x)
   */
  def chiSquareQuantiles(prob: Double, degOfFreedom: Double, tail: Boolean = true): Double

}