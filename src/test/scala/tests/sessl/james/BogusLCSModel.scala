package tests.sessl.james

import james.core.base.INamedEntity
import examples.sr.LinearChainSystem
import james.core.model.AccessRestriction
import james.core.observe.IMediator
import tests.sessl.TestCounter

/**
 * LCS Model enhanced by calls to TestCounter.
 * 
 * @author Roland Ewald
 *
 */
class BogusLCSModel(params: java.util.Map[java.lang.String, java.lang.Object]) extends LinearChainSystem(params) {

  println("MODEL SETUP:")
  TestCounter.registerParamCombination(params)
  val i = params.keySet.iterator()
  while (i.hasNext()) {
    val s = i.next()
    println(s + " => " + params.get(s))
  }
  println("\n\n\n")

}