package sessl
import scala.collection.mutable.ListBuffer
import java.net.URI

/** Support for configuring the model. Supports to set fixed model parameters (via define(...)) as well as parameter scans (via scan(...)).
 *  @author Roland Ewald
 */
trait SupportModelConfiguration {

  /** The model to be used. */
  protected[this] var modelLocation: Option[String] = None

  /** List of experiment variables to be scanned. */
  private[this] val varsToScan = ListBuffer[Variable]()

  /** List of experiment variables to be set (for each run). */
  private[this] val varsToSet = ListBuffer[VarSingleVal]()

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
  protected lazy val variablesToScan = varsToScan.toList

  /** Get all defined variables that shall be fixed.*/
  protected lazy val variablesToSet = varsToSet.toList
  
  /** Get all defined variables that shall be fixed as a map. */
  protected lazy val fixedVariables = variablesToSet.map(v => (v.name, v.value)).toMap

  /** Allow to specify a model URI. */
  def model_=(modelURI: URI) = { modelLocation = Some(modelURI.toString()) }
  /** Default getters and setters. */
  protected def model_=(modelString: String) = { modelLocation = Some(modelString) }
  protected def model: String = modelLocation.get

}