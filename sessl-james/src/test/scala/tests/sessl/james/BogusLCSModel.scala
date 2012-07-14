/*******************************************************************************
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
 ******************************************************************************/
package tests.sessl.james

import examples.sr.LinearChainSystem
import sessl.util.test.TestCounter
import sessl.util.Logging

/**
 * LCS Model enhanced by calls to TestCounter.
 *
 * @author Roland Ewald
 *
 */
class BogusLCSModel(params: java.util.Map[java.lang.String, java.lang.Object]) extends LinearChainSystem(params) with Logging {

  TestCounter.registerParamCombination(params)
  logger.info("MODEL SETUP:" + scala.collection.JavaConversions.mapAsScalaMap(params).mkString(",") + "\n\n\n")
}
