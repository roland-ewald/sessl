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

import org.junit.Test
import org.junit.Assert._

/** Tests whether writing to the database works.
 *  @author Roland Ewald
 */
class TestDataSink {

  @Test def testDBDataSinkExperiment() = {
    import sessl._
    import sessl.james._

    val exp = new Experiment with Observation with DataSink {
      model = TestJamesExperiments.testModel
      stopTime = 0.1
      dataSink = MySQLDataSink(schema = "test", password = "")
    }
    execute(exp)
  }
}
