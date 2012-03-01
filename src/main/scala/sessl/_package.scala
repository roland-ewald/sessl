/**
 * Some general definitions.
 *
 * @author Roland Ewald
 *
 */
package object sessl {

  /** Type for time-stamped data. */
  type TimeStampedData = (Double, _)

  /** Type for trajectories. */
  type Trajectory = List[TimeStampedData]

  /** The type to represent variable assignments. List should be sorted lexicographically by variable name. */
  type VariableAssignment = List[(String, Any)]

  /**
   * Support for map-like syntax to define experiment variables.
   * @param name the string to be interpreted as a variable name
   * @return the VarName instance
   */
  implicit def stringToVarName(name: String): VarName = new VarName(name)

  /**
   *  Support for map-like syntax to define parameters.
   *  @param name the string to be interpreted as a variable name
   *  @return the ParameterName instance
   */
  implicit def stringToParamName(name: String) = new ParamName(name)

  /**
   * Support for variable name bindings via 'as'.
   * @param name the name of the observable on the sessl level
   * @return a data element name instance
   */
  implicit def stringToDataElementName(name: String) = new DataElemName(name)

  /**
   * Support for variables to be converted in a one-element list of 'multiple' variables (to combine them).
   * @param v the variable
   * @return a one-element list of 'multiple' variables
   */
  implicit def varToMultiVar(v: Variable) = new MultipleVars(List(v))

  /**
   * Support for combining optimization stop policies.
   * @param p the optimization stop policy
   * @return a 'combined' policy with just the left-hand side defined
   */
  implicit def optStopPolicyToCombinedPolicy(p: OptimizationStopPolicy) = new CombinedOptimizationStopPolicy(p)

  /**
   * Support for combining stopping criteria.
   * @param s the stopping criterion
   * @return a 'combined' stopping criterion with just the left-hand side defined
   */
  implicit def stopCriterionToCombinedCriterion(s: StoppingCriterion) = new CombinedStoppingCriterion(s)

  /**
   * Support for combining replication criteria.
   * @param r the replication criterion
   * @return a 'combined' replication criterion with just the left-hand side defined
   */
  implicit def replCriterionToCombinedCriterion(r: ReplicationCriterion) = new CombinedReplicationCriterion(r)

  /**
   * Execute experiments sequentially.
   * @param exps the experiments to be executed
   */
  def execute(exps: AbstractExperiment*) = AbstractExperiment.execute(exps: _*)

}