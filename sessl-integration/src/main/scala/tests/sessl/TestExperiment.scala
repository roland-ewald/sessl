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
package tests.sessl

import sessl._
import sessl.james._

/** Test experiment for optimization. */
class TestExperiment extends Experiment with Observation with ParallelExecution {
  model = "java://examples.sr.LinearChainSystem"
  stopTime = 1.0
  replications = 2
  observe("x" ~ "S0", "y" ~ "S1")
  observeAt(0.8)
}