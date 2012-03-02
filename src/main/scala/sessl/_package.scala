/** Some general definitions.
 *
 *  @author Roland Ewald
 *
 */
package object sessl {

  /** Type for time-stamped data. */
  type TimeStampedData = (Double, _)

  /** Type for trajectories. */
  type Trajectory = List[TimeStampedData]

  /** The type to represent variable assignments. List should be sorted lexicographically by variable name. */
  type VariableAssignment = List[(String, Any)]

  /** Support for map-like syntax to define experiment variables.
   *  @param name the string to be interpreted as a variable name
   *  @return the VarName instance
   */
  implicit def stringToVarName(name: String): VarName = new VarName(name)

  /** Support for map-like syntax to define parameters.
   *  @param name the string to be interpreted as a variable name
   *  @return the ParameterName instance
   */
  implicit def stringToParamName(name: String) = new ParamName(name)

  /** Support for variable name bindings via 'as'.
   *  @param name the name of the observable on the sessl level
   *  @return a data element name instance
   */
  implicit def stringToDataElementName(name: String) = new DataElemName(name)

  /** Support for variables to be converted in a one-element list of 'multiple' variables (to combine them).
   *  @param v the variable
   *  @return a one-element list of 'multiple' variables
   */
  implicit def varToMultiVar(v: Variable) = new MultipleVars(List(v))

  /** Support for combining optimization stop policies.
   *  @param p the optimization stop policy
   *  @return a 'combined' policy with just the left-hand side defined
   */
  implicit def optStopPolicyToCombinedPolicy(p: OptimizationStopPolicy) = new CombinedOptimizationStopPolicy(p)

  /** Support for combining stopping conditions.
   *  @param s the stopping condition
   *  @return a 'combined' stopping condition with just the left-hand side defined
   */
  implicit def stopConditionToCombinedCondition(s: StoppingCondition) = new CombinedStoppingCondition(s)

  /** Support for combining replication conditions.
   *  @param r the replication condition
   *  @return a 'combined' replication condition with just the left-hand side defined
   */
  implicit def replConditionToCombinedCondition(r: ReplicationCondition) = new CombinedReplicationCondition(r)

  /** Execute experiments sequentially.
   *  @param exps the experiments to be executed
   */
  def execute(exps: AbstractExperiment*) = AbstractExperiment.execute(exps: _*)

}