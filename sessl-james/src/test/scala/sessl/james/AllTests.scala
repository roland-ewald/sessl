package sessl.james

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import tests.sessl.james.TestJamesExperiments
import tests.sessl.james.TestRNGSetup
import tests.sessl.james.TestPerformanceExperiments
import tests.sessl.james.Examples
import tests.sessl.james.TestParameterScan
import tests.sessl.james.TestJamesExperiments
import tests.sessl.james.TestOptimization
import tests.sessl.james.TestDataSink
import tests.sessl.james.TestReplicationConditions
import tests.sessl.james.TestBiBJamesExperiment
import tests.sessl.james.TestStoppingConditions
import tests.sessl.james.TestReport
/**
 * Bundles all tests together (as the Eclipse-JUnit-Runner cannot easily deal with Scala code).
 *  @author Roland Ewald
 */
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[TestPerformanceExperiments], classOf[ParamBlockGeneratorTest],
  classOf[Examples], classOf[TestRNGSetup], classOf[TestParameterScan], classOf[TestOptimization],
  classOf[TestRNGSetup], classOf[TestOptimization], classOf[TestDataSink], classOf[TestBiBJamesExperiment], classOf[TestReport],
  classOf[TestStoppingConditions], classOf[TestReplicationConditions]))
class AllTests