package sessl

import sessl.util.Logging

/**
 * Support for configuring the number of desired replications.
 *  @author Roland Ewald
 */
trait SupportReplicationConditions extends Logging {

  /** Stores the fixed number of replications to be executed (if one is set). */
  protected[sessl] var fixedReplications: Option[Int] = None

  /** Stores a more complex replication condition (if one is set). */
  protected[sessl] var repCondition: Option[ReplicationCondition] = None

  /** Getting/setting the number of replications. */
  def replications_=(reps: Int) = { fixedReplications = Some(reps) }
  def replications: Int = fixedReplications.get

  /** Getting/setting a complex replication condition. */
  def replicationCondition_=(rc: ReplicationCondition) = {
    if (repCondition.isDefined)
      logger.warn("Replication condition has already been defined as '" +
        repCondition.get + "' - this will now be changed to '" + rc + "'")
    repCondition = Some(rc)
  }
  def replicationCondition: ReplicationCondition = repCondition.get

  /**
   * Check replication setup and get replication condition.
   *  @return the replication condition to be used
   */
  protected[sessl] def checkAndGetReplicationCondition() = {
    require(!(fixedReplications.isDefined && repCondition.isDefined),
      "Both a fixed number of replications (" + fixedReplications.get + ") and a replication condition (" +
        repCondition.get + ") are set - only one is allowed. Use '" + FixedNumber(fixedReplications.get) +
        "' to add the fixed number of replications to the condition.")
    if (repCondition.isDefined)
      repCondition.get
    else if (fixedReplications.isDefined)
      FixedNumber(fixedReplications.get)
    else FixedNumber(1)
  }
}

//class hierarchy for replication conditions

/** Super type of all replication conditions. */
trait ReplicationCondition

/**
 * Replicate a fixed number of times.
 *  @param replications the number of replications
 */
case class FixedNumber(replications: Int) extends ReplicationCondition

/**
 * Replicate until a certain confidence in the mean value of a variable is reached.
 *  @param varName the (sessl) name of the variable under scrutiny
 *  @param  relativeHalfWidth the desired relative half-width of the confidence interval
 *  @param confidence the confidence in the interval
 */
case class MeanConfidenceReached(varName: String, relativeHalfWidth: Double = 0.01, confidence: Double = 0.95) extends ReplicationCondition

/**
 * Stop if both given conditions are fulfilled.
 *  @param left the first replication condition
 *  @param right the second replication condition
 */
case class ConjunctiveReplicationCondition(left: ReplicationCondition, right: ReplicationCondition) extends ReplicationCondition

/**
 * Stop if any of the given conditions is fulfilled.
 *  @param left the first replication condition
 *  @param right the second replication condition
 */
case class DisjunctiveReplicationCondition(left: ReplicationCondition, right: ReplicationCondition) extends ReplicationCondition

/**
 * Combines two replication conditions (either with OR or with AND).
 *  @param left the first replication condition
 */
case class CombinedReplicationCondition(left: ReplicationCondition) extends ReplicationCondition {
  def or(right: ReplicationCondition) = new DisjunctiveReplicationCondition(left, right)
  def and(right: ReplicationCondition) = new ConjunctiveReplicationCondition(left, right)
}