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
      dataSink = MySQLDataSink(schema = "test")
    }
    execute(exp)
  }
}