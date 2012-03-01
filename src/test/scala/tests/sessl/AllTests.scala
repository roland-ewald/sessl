package tests.sessl

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Assert._
import org.junit.runners.Suite.SuiteClasses
import james.TestJamesExperiments
import james.TestPerformanceExperiments
import sessl.TestSimpleInstrumentation
import sessl.james.ParamBlockGeneratorTest

/** Bundles all tests together (as the Eclipse-JUnit-Runner cannot easily deal with Scala code).
 *  @author Roland Ewald
 *
 */
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[TestJamesExperiments], classOf[TestPerformanceExperiments], classOf[TestVariable],
  classOf[TestSimpleInstrumentation], classOf[ParamBlockGeneratorTest]))
class AllTests {

}