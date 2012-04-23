package sessl

import org.junit.Test
import junit.framework.Assert._
import com.sun.media.sound.SimpleInstrument
import sessl.reference.EmptyExperiment

/**
 *
 * @see sessl.SimpleInstrumentation
 *
 * @author roland
 *
 */
@Test
class TestSimpleInstrumentation {

  val varName = "testVar"

  val boundVarName = "var"

  val value = 12

  val time = .1

  val runID = 1

  @Test
  def testSimpleInstr() = {
    val testObject = new EmptyExperiment //TODO: Make an EmptyExperiment Object out of this!
    testObject.observe(boundVarName ~ varName)

    testObject.addValueFor(runID, varName, (time, value))
    val collectedResults = testObject.collectResults(1)
    assertEquals(value, collectedResults(boundVarName))
  }

}