/**
 * *****************************************************************************
 * Copyright 2013 Roland Ewald
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
package sessl.opt4j

import org.opt4j.core.Objective.Sign
import org.opt4j.core.Objectives
import org.opt4j.core.problem.Evaluator

import sessl.optimization.MultiObjective
import sessl.optimization.Objective
import sessl.optimization.OptDirection
import sessl.optimization.SimpleParameters
import sessl.optimization.SingleObjective
import sessl.util.Logging

/**
 * Evaluates phenotypes.
 *
 * @see org.opt4j.core.problem.Evaluator
 *
 * @author Roland Ewald
 */
class SimpleParameterEvaluator extends Evaluator[SimpleParameters] with Logging {

  implicit def optDirectionToSign(d: OptDirection) = if (d == sessl.optimization.min) Sign.MIN else Sign.MAX

  override def evaluate(params: SimpleParameters): Objectives = {
    val objectives: Objectives = new Objectives

    val newObjective = Opt4JSetup.copyObjective()
    Opt4JSetup.eval(params, newObjective)

    newObjective match {
      case obj: SingleObjective => objectives.add("objective", obj.direction, obj.singleValue)
      case obj: MultiObjective => obj.dimensions.foreach { d =>
        objectives.add(d._1, d._2, obj.value(d._1))
      }
    }

    if (params.firstUnusedParameter >= 0)
      logger.warn("The parameter '" + params.firstUnusedParameterName.get +
        "' has not been accessed from within the objective function. Is the configuration of the search space correct?")
    objectives
  }
}