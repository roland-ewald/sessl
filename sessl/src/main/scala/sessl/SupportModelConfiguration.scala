/**
 * *****************************************************************************
 * Copyright 2012 Roland Ewald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package sessl
import scala.collection.mutable.ListBuffer
import java.net.URI

/**
 * Support for configuring the model. Supports to set fixed model parameters (via `set(...)`) as well as parameter scans (via `scan(...)`).
 *  @author Roland Ewald
 */
trait SupportModelConfiguration {

  /** The model to be used. */
  protected[this] var modelLocation: Option[String] = None

  /** List of experiment variables to be scanned. */
  private[this] val varsToScan = ListBuffer[Variable]()

  /** List of experiment variables to be set (for each run). */
  private[this] val varsToSet = ListBuffer[VarSingleVal]()

  /**
   * Define the experiment variables to be scanned.
   *  @param variablesToScan a list of variables to scan, concatenate with 'and' to make them change values in unison (but only if the same number of values shall be scanned for them)
   *
   *  @example {{{
   *    scan ("x" <~ range(0,2,8)) // model runs with x = 0, 2, 4, 6, 8
   *  }}}
   *  @example {{{
   *    scan ("x" <~ range(1,1,10), "y" <~ range(2,2,20)) // model runs with (x,y) = (1,2), (1,4), ..., (1,20), (2,2), (2,4), ... (10,20)
   *  }}}
   *  @example {{{
   *    scan ("x" <~ range(1,1,10) and "y" <~ range(2,2,20)) // model runs with (x,y) = (1,2), (2,4), ..., (9,18), (10,20)
   *  }}}
   *  @example {{{
   *    scan ("x" <~ (1,2,3) and "y" <~ ("a", "b", "c"), "z" <~ range(1,1,100)) // model runs with (x,y,z) = (1,"a", 1), ..., (1,"a", 100), (2, "b", 1), ... (2,"b", 100), ... etc.
   *  }}}
   */
  def scan(variablesToScan: Variable*) = {
    varsToScan ++= variablesToScan
  }

  /**
   * Define the variables to be set for each run (fixed).
   *  @param variablesToSet a list of variables to set
   *  @example {{{
   *     set("x" <~ 1.0, "y" <~ 35, "z" <~ "something else")
   *  }}}
   */
  def set(variablesToSet: Variable*) {
    for (variable <- variablesToSet)
      variable match {
        case v: VarSingleVal => varsToSet += v
        case v: Variable => throw new IllegalArgumentException("Variable '" + v.name + "' does not specify a single value!")
      }
  }

  /** Creates variable setups (or list with single empty map, if none are defined). */
  private[sessl] def createVariableSetups(): List[Map[String, Any]] = {
    if (!variablesToScan.isEmpty)
      Variable.createVariableSetups(variablesToScan).toList
    else List(Map())
  }

  /** Get all defined variables that shall be scanned.*/
  protected lazy val variablesToScan = varsToScan.toList

  /** Get all defined variables that shall be fixed.*/
  protected lazy val variablesToSet = varsToSet.toList

  /** Get all defined variables that shall be fixed as a map. */
  protected lazy val fixedVariables = variablesToSet.map(v => (v.name, v.value)).toMap

  /** Allow to specify a model URI. */
  def model_=(modelURI: URI) = { modelLocation = Some(modelURI.toString()) }

  /** Set string identifying the model to be simulated. */
  protected def model_=(modelString: String) = { modelLocation = Some(modelString) }
  
  /** Get string identifying the model to be simulated. */
  def model: String = modelLocation.get
}
