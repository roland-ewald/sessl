package sessl.sbw

import org.junit.Test
import edu.caltech.sbw.SBW

class TestSBWFunctionality {

  @Test def testSimpleSBWLookup() = {
	  SBW.connect
	  for (m <- SBW.getExistingModuleInstances())
	    println(m.getDescriptor().getName())
  }

}