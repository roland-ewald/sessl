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

import org.opt4j.core.optimizer.Archive
import org.opt4j.core.optimizer.OptimizerIterationListener
import org.opt4j.core.optimizer.Population
import com.google.inject.Inject

/**
 * Listens to the end of an iteration within the optimizer and calls the corresponding event handling
 * methods from {@link Opt4JSetup}.
 *
 * @see Opt4JSetup
 *
 * @author Roland Ewald
 */
class IterationListener @Inject() (val population: Population, val archive: Archive) extends OptimizerIterationListener {

  override def iterationComplete(iteration: Int) = if (iteration > 1) Opt4JSetup.iterationComplete(population, archive)

}
