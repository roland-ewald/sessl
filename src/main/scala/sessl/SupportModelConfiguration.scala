package sessl
import scala.collection.mutable.ListBuffer

/**
 * Support for configuring the model. Supports to set fixed model parameters (via define(...)) as well as parameter scans (via scan(...)).
 * @author Roland Ewald
 */
trait SupportModelConfiguration {

  /** List of experiment variables to be scanned. */
  private[this] val varsToScan = ListBuffer[Variable]()

  /** List of experiment variables to be set (for each run). */
  private[this] val varsToSet = ListBuffer[Variable]()

  /** Define the experiment variables to be scanned. */
  def scan(variablesToScan: Variable*) = {
    varsToScan ++= variablesToScan
  }

  /** Define the variables to be set for each run (fixed). */
  def define(variablesToSet: Variable*) {
    for (variable <- variablesToSet)
      variable match {
        case v: VarSingleVal => varsToSet += v
        case v: Variable => throw new IllegalArgumentException("Variable '" + v.name + "' does not specify a single value!")
      }
  }

  /** Get all defined variables that shall be scanned.*/
  protected def variablesToScan = varsToScan.toList

  /** Get all defined variables that shall be fixed.*/
  protected def variablesToSet = varsToSet.toList

}