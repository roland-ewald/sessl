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
import tests.sessl.james.TestParameterScan
import tests.sessl.james.TestOptimization
import james.TestRNGSetup
import tests.sessl.james.TestDataSink
import tests.sessl.james.TestBiBJamesExperiment
import tests.sessl.james.TestReport
import tests.sessl.james.TestStoppingConditions
import tests.sessl.james.TestReplicationConditions
import tests.sessl.sbmlsim.TestSimpleSBMLSimExperiments

/** Bundles all tests together (as the Eclipse-JUnit-Runner cannot easily deal with Scala code).
 *  @author Roland Ewald
 */
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[TestParameterScan], classOf[TestOptimization], classOf[TestRNGSetup],
  classOf[TestDataSink], classOf[TestBiBJamesExperiment], classOf[TestReport], classOf[TestStoppingConditions],
  classOf[TestReplicationConditions], classOf[TestPerformanceExperiments],
  classOf[TestVariable], classOf[TestSimpleInstrumentation], classOf[ParamBlockGeneratorTest], classOf[TestSimpleSBMLSimExperiments]))
class AllTests {

}