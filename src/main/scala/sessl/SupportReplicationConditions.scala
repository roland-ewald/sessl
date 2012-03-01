package sessl

/** Support for configuring the number of desired replications.
 *  @author Roland Ewald
 */
trait SupportReplicationConditions {

  /** Stores the fixed number of replications to be executed (if one is set). */
  protected[sessl] var fixedReplications: Option[Int] = None

  /** Stores a more complex replication criterion (if one is set). */
  protected[sessl] var replicationCriterion: Option[ReplicationCriterion] = None

  /** Getting/setting the number of replications. */
  def replications_=(reps: Int) = { fixedReplications = Some(reps) }
  def replications: Int = fixedReplications.get

  /** Getting/setting a complex replication condition. */
  def replicationCondition_=(rc: ReplicationCriterion) = {
    if (replicationCriterion.isDefined)
      println("Warning: replication criterion has already been defined as '" + replicationCriterion.get + "' - this will now be changed to '" + rc + "'") //TODO: Use logging
    replicationCriterion = Some(rc)
  }
  def replicationCondition: ReplicationCriterion = replicationCriterion.get

  /** Check replication setup and get replication criterion.
   *  @return the replication criterion to be used
   */
  protected[sessl] def checkAndGetReplicationCriterion() = {
    require(!(fixedReplications.isDefined && replicationCriterion.isDefined), "Both a fixed number of replications (" + fixedReplications.get + ") and a replication condition (" +
      replicationCriterion.get + ") are set - only one is allowed. Use '" + FixedNumber(fixedReplications.get) + "' to add the fixed number of replications to the condition.")
    if (replicationCriterion.isDefined)
      replicationCriterion.get
    else if (fixedReplications.isDefined)
      FixedNumber(fixedReplications.get)
    else FixedNumber(1)
  }
}

//class hierarchy for replication criteria

/** Super type of all replication criteria. */
trait ReplicationCriterion

/** Replicate a fixed number of times.
 *  @param replications the number of replications
 */
case class FixedNumber(replications: Int) extends ReplicationCriterion

/** Replicate until a certain confidence in the mean value of a variable is reached.
 *  @param varName the (sessl) name of the variable under scrutiny
 *  @param  relativeHalfWidth the desired relative half-width of the confidence interval
 *  @param confidence the confidence in the interval
 */
case class MeanConfidenceReached(varName: String, relativeHalfWidth: Double = 0.01, confidence: Double = 0.95) extends ReplicationCriterion

/** Stop if both given criteria are fulfilled.
 *  @param left the first replication criterion
 *  @param right the second replication criterion
 */
case class ConjunctiveReplicationCriterion(left: ReplicationCriterion, right: ReplicationCriterion) extends ReplicationCriterion

/** Stop if any of the given criteria is fulfilled.
 *  @param left the first replication criterion
 *  @param right the second replication criterion
 */
case class DisjunctiveReplicationCriterion(left: ReplicationCriterion, right: ReplicationCriterion) extends ReplicationCriterion

/** Combines two replication criteria (either with OR or with AND).
 *  @param left the first replication criterion
 */
case class CombinedReplicationCriterion(left: ReplicationCriterion) extends ReplicationCriterion {
  def or(right: ReplicationCriterion) = new DisjunctiveReplicationCriterion(left, right)
  def and(right: ReplicationCriterion) = new ConjunctiveReplicationCriterion(left, right)
}