package sessl.sbw

import org.junit.Test
import edu.caltech.sbw.SBW
import org.junit.Assume
import org.junit.Before
import org.junit.Assert._

class TestSBWFunctionality {

  /** Checks whether there is an SBW installation to use. */
  val connectionSuccessful =
    try {
      SBW.connect
      true
    } catch {
      case _: Throwable => false
    }

  @Before def setUp() = {
    Assume.assumeTrue(connectionSuccessful);
  }

  @Test def testSimpleSBWLookup() = {
    assertTrue("There are modules.", SBW.getExistingModuleInstances().length > 0)
    for (m <- SBW.getExistingModuleInstances())
      println(m.getDescriptor().getName())
  }

}