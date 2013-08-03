package sessl.sbw

import org.junit.Assume
import edu.caltech.sbw.SBW
import org.junit.Before
import org.junit.After

class SBWTest {

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

  @After def tearDown() = {
    if (connectionSuccessful)
      SBW.disconnect()
  }

}