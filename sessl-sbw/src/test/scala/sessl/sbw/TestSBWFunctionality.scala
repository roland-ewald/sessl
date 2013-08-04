package sessl.sbw

import org.junit.Test
import edu.caltech.sbw.SBW
import org.junit.Assume._
import org.junit.Before
import org.junit.Assert._
import org.junit.After
import sessl.sbw.util.SBWModuleDirectory

trait Analysis {
  def doAnalysis(sbml: String): Unit
}

class TestSBWFunctionality extends SBWTest {

  val targetService = "General Simulation Tool"

  @Test def testSimpleSBWLookup() = {
    assertTrue("There are modules.", SBW.getExistingModuleInstances().length > 0)
    
    val targetModuleInstance = SBW.findServices("Analysis", false).filter(_.getDisplayName() == targetService).headOption

    assumeTrue(targetModuleInstance.isDefined)

    val sbwDir = new SBWModuleDirectory()

    // From documentation:
    val service = targetModuleInstance.get.getServiceInModuleInstance()
    
    for (m <- service.getMethods())
      println(m.getSignatureString())
    
    val analysis = service.getServiceObject(classOf[Analysis]).asInstanceOf[Analysis]
    analysis.doAnalysis("<test></test>")
  }

}