package tests.sessl.james

import examples.sr.LinearChainSystem
import sessl.util.test.TestCounter
import sessl.util.Logging
import sessl.util.JavaToScala.toScala

/**
 * LCS Model enhanced by calls to TestCounter.
 *
 * @author Roland Ewald
 *
 */
class BogusLCSModel(params: java.util.Map[java.lang.String, java.lang.Object]) extends LinearChainSystem(params) with Logging {

  TestCounter.registerParamCombination(params)
  logger.info("MODEL SETUP:" + toScala(params).mkString(",") + "\n\n\n")

}