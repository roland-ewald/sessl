package tests.sessl

import org.junit.runners.Suite.SuiteClasses
import org.junit.runners.Suite
import org.junit.runner.RunWith
import sessl.util.TestInterpolation
import sessl.TestSimpleInstrumentation

/**
 * Bundles all tests together (as the Eclipse-JUnit-Runner cannot easily deal with Scala code).
 *  @author Roland Ewald
 */
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[TestInterpolation], classOf[TestVariable], classOf[TestSimpleInstrumentation]))
class AllTests