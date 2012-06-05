/** Basic support for variables.
 *  @author Roland Ewald
 */
package sessl

import scala.math.Numeric._
import scala.collection.mutable.ListBuffer

/** Super class for all experiment variables. */
sealed trait Variable {

  /** The name of the variable. */
  def name: String

  /** The value of the variable. */
  def value: AnyRef

}

/** Methods applicable to variables*/
object Variable {

  /** Creates the setups for multiple variables.
   *
   *  @param variables the list of variables
   *  @param givenSetups the given setups
   *  @return the created setups
   */
  def createMultipleVarsSetups(variables: List[Variable], givenSetups: Seq[Map[String, Any]] = Seq(Map())) = {
    val allNewSetups = variables.map(v => createVariableSetups(Seq(v), Seq(Map[String, AnyRef]())))
    require(!allNewSetups.isEmpty && allNewSetups.forall(_.length == allNewSetups(0).length), "The number of generated setups needs to be the same.")

    val newSetups = allNewSetups(0).toArray
    for (newSetup <- allNewSetups.tail; i <- newSetup.indices)
      newSetups(i) = newSetups(i) ++ newSetup(i)

    createCombinations(givenSetups, newSetups)
  }

  /** Creates all defined variable setups.
   *
   *  @param variablesToScan the variables to scan
   *  @return the sequence of defined setups
   */
  protected[sessl] def createVariableSetups(variablesToScan: Seq[Variable]): Seq[Map[String, Any]] = {
    val varsToScan = variablesToScan.map(v => (v.name, v)).toMap
    createVariableSetups(variablesToScan, Seq(Map()))
  }

  /** Creates the variable setups.
   *
   *  @param variables the list of all variables
   *  @param givenSetups the setups already given
   *  @return the created variable setups
   */
  private[this] def createVariableSetups(variables: Seq[Variable], givenSetups: Seq[Map[String, Any]]): Seq[Map[String, Any]] = {

    if (variables.isEmpty)
      return givenSetups

    val rv = variables.head match {
      case mv: MultipleVars => createMultipleVarsSetups(mv.variables, givenSetups)
      case vr: VarRange[_] => createCombinations(givenSetups, VarRange.toList(vr).map(value => Map(vr.name -> value)))
      case vs: VarSeq => createCombinations(givenSetups, vs.values.map(value => Map(vs.name -> value)))
      case v: VarSingleVal => createCombinations(givenSetups, Seq(Map(v.name -> v.value)))
      case x => throw new IllegalArgumentException("Variable " + x + " is not supported.")
    }

    createVariableSetups(variables.tail, rv)
  }

  /** Yields all combinations of two sequences of maps.
   *  @param seq1 the first sequence of maps
   *  @param seq2 the second sequence of maps
   *  @return a sequence with all combinations of maps
   */
  private[this] def createCombinations[X, Y](seq1: Seq[Map[X, Y]], seq2: Seq[Map[X, Y]]): Seq[Map[X, Y]] =
    for (map1 <- seq1; map2 <- seq2) yield (map1 ++ map2)

}

/** Class that represents a semi-defined variable (only variable name is given so far). */
case class VarName(name: String) {
  def <~[T](values: T*) = {
    if (values.size == 1) {
      values(0) match {
        case r: ValueRange[_] => VarRange(name, r.from, r.step, r.to)
        case s: Seq[_] => VarSeq(name, s)
        case _ => VarSingleVal(name, values(0).asInstanceOf[AnyRef])
      }
    } else VarSeq(name, values.asInstanceOf[Seq[AnyRef]].toList)
  }
}

/** This represents a range of possible values (for a variable).
 *
 *  @param from
 *            the lower boundary
 *  @param step
 *            the step size
 *  @param to
 *            the upper boundary (inclusive)
 */
case class ValueRange[T <: AnyVal](from: T, step: T, to: T) {

  /** Converts a range to a list of values.
   *
   *  @param <T> the generic type
   *  @param r the range
   *  @param n the numeric aspect of T
   *  @return the list of concrete values
   */
  def toList(implicit n: Numeric[T]): List[T] = {

    require(!n.eq(step, n.zero), "Step size must be != zero.")
    val posStepSize = n.gt(step, n.zero)

    require(n.lteq(from, to) || !posStepSize, "Lower bound (" + from + ") > upper bound (" + to + "), while having a positive step size (" + step + ")!")
    require(n.gteq(from, to) || posStepSize, "Lower bound (" + from + ") < upper bound (" + to + "), while having a negative step size (" + step + ")!")

    val rangeValues = ListBuffer[T]()
    rangeValues += from
    var nextValue = n.plus(from, step)
    while ((n.lteq(nextValue, to) && posStepSize) || (n.gteq(nextValue, to) && !posStepSize)) {
      rangeValues += nextValue
      nextValue = n.plus(nextValue, step)
    }
    rangeValues.toList
  }

}

/** Provides syntactic sugar for declaration of value ranges.  */
object range {

  /** Specify a range of values. */
  def apply[T <: AnyVal](from: T, step: T, to: T) = ValueRange(from, step, to)

  /** Simplifying constructor, sets step size to one. */
  def apply[T <: AnyVal](from: T, to: T)(implicit n: Numeric[T]) = ValueRange(from, n.one, to)
}

/** A variable associated with a range of values.
 *
 *  @param <T>
 *            the generic type
 *  @param name the name of the variable
 *  @param from the lower bound of the variable
 *  @param step the step size
 *  @param to the upper bound of the variable (inclusive)
 */
case class VarRange[T <: AnyVal](name: String, from: T, step: T, to: T) extends Variable {

  override def value = range(from, step, to)

  /** Generate list of values specified by range. */
  def toList(implicit n: Numeric[T]) = value.toList(n)

}

object VarRange {

  /** Generate list of values from range. */
  def toList(varRange: VarRange[_]): List[AnyVal] = {
    if (varRange.from.isInstanceOf[Int])
      return varRange.asInstanceOf[VarRange[Int]].toList
    else if (varRange.from.isInstanceOf[Short])
      return varRange.asInstanceOf[VarRange[Short]].toList
    else if (varRange.from.isInstanceOf[Long])
      return varRange.asInstanceOf[VarRange[Long]].toList
    else if (varRange.from.isInstanceOf[Double])
      return varRange.asInstanceOf[VarRange[Double]].toList
    throw new IllegalArgumentException("Listing values not supported for variable range '" + varRange + "'.")
  }
}

/** Sequence of elements. */
case class VarSeq(name: String, value: Seq[_]) extends Variable {
  def values = value
}

/** Single element. */
case class VarSingleVal(name: String, value: AnyRef) extends Variable

/** Multiple variables on the same 'layer' of the experiment variables.*/
case class MultipleVars(value: List[Variable]) extends Variable {

  override def name = value.map(_.name).mkString(", ")

  /** Adding two (lists of) variables yields a new list of variables (a concatenation).
   *  @param that the other variable
   */
  def and(that: MultipleVars) = new MultipleVars(this.value ::: that.value)

  def variables = value
}