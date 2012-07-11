package tests.sessl.james

import examples.sr.LinearChainSystem
import sessl.util.test.TestCounter
import sessl.util.Logging

/**
 * LCS Model enhanced by calls to TestCounter.
 *
 * @author Roland Ewald
 *
 */
class BogusLCSModel(params: java.util.Map[java.lang.String, java.lang.Object]) extends LinearChainSystem(params) with Logging {

  TestCounter.registerParamCombination(params)
  logger.info("MODEL SETUP:" + scala.collection.JavaConversions.mapAsScalaMap(params).mkString(",") + "\n\n\n")
}