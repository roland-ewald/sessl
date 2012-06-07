package sessl.james.tools

import sessl.Trajectory
import sessl.ObservationReplicationsResultsAspect
import sessl.ObservationRunResultsAspect
import sessl.ObservationReplicationsResultsAspect
import james.core.math.statistics.tests.wilcoxon.WilcoxonRankSumTest

/**
 * Simple component to compare two sets of trajectories.
 *
 *  @author Roland Ewald
 */
object TrajectorySetsComparator {

  /** A 'slice' of a trajectory set, i.e. the empirical distribution at a given point in time. */
  type Slice = List[Double]

  /**
   * Compares two time series (with equal number of measurements at approximately the same time points) by applying a statistical test to
   *  their empirical distributions for each time point.
   */
  def compare(referenceData: ObservationReplicationsResultsAspect, testData: ObservationReplicationsResultsAspect, varName: String) = {
    val referenceSlices = sliceTrajectories(getTrajectorySet(referenceData, varName))
    val testSlices = sliceTrajectories(getTrajectorySet(testData, varName))
    require(referenceSlices.size == testSlices.size, "Number of reference and test slices need to be equal (are all trajectories of the same length?).")
    for ((refSlice, testSlice) <- referenceSlices zip testSlices) yield new WilcoxonRankSumTest().executeTest(toDoubleList(refSlice), toDoubleList(testSlice))
  }

  /** Retrieves list of trajectories from result aspect. */
  private[this] def getTrajectorySet(aspect: ObservationReplicationsResultsAspect, varName: String) =
    aspect.runsResults.map(_._2.asInstanceOf[ObservationRunResultsAspect].trajectory(varName))

  /** 'Slices' a set of trajectories. */
  private[this] def sliceTrajectories(trajectories: Iterable[Trajectory]): Iterable[Iterable[Double]] = {
    for (i <- 0 until trajectories.head.size) yield {
      for (t <- trajectories) yield t(i)._2.asInstanceOf[Number].doubleValue()
    }
  }

}