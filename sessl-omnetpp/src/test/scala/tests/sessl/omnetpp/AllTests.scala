package tests.sessl.omnetpp

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

/**
 * Bundles together all tests for the OMNeT++ binding.
 * @author Roland Ewald
 */
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[SimpleOMNeTPPExperiments], classOf[TestResultFileParser],
  classOf[TestResultReader]))
class AllTests