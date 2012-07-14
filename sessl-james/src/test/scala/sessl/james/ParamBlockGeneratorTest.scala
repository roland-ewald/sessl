/*******************************************************************************
 * Copyright 2012 Roland Ewald
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package sessl.james

import james.core.util.eventset.plugintype.EventQueueFactory
import sessl._
import org.junit.Assert._
import org.junit.Test
import sessl.util.AlgorithmSet
import sessl.util.Logging

/**
 * Tests for {@link ParamBlockGenerator}.
 *
 *  @author Roland Ewald
 */
@Test class ParamBlockGeneratorTest extends Logging {

  /** Tests the conversion of sessl algorithm setups to parameter blocks. */
  @Test def testAlgorithmSetConversion() {
    val tlSetups = AlgorithmSet[BasicJamesIISimulator](TauLeaping(gamma = 11) scan { "epsilon" <~ range(0.01, 0.01, 0.03) })
    assertEquals("There should be three simulator setups.", 3, tlSetups.size)
    val tlParamBlocks = ParamBlockGenerator.createParamBlockSet(tlSetups)
    testSize(tlSetups.size, tlParamBlocks.size)
    testFactoryClassSetup(TauLeaping().factory, tlParamBlocks)

    val nrSetups = AlgorithmSet[BasicJamesIISimulator](NextReactionMethod() scan { "eventQueue" <~ (SortedList(), LinkedList()) })
    assertEquals("There should be two simulator setups.", 2, nrSetups.size)
    val nrParamBlocks = ParamBlockGenerator.createParamBlockSet(nrSetups)
    testSize(nrSetups.size, nrParamBlocks.size)
    testFactoryClassSetup(NextReactionMethod().factory, nrParamBlocks)
    testFactoryClassInSubBlocks(classOf[EventQueueFactory].getName(), nrParamBlocks)
    logger.info(nrParamBlocks.mkString(", "))
  }

  /** Compare the size of algorithm- and parameter block set. */
  def testSize(algoSetSize: Int, paramBlockSetSize: Int) = assertEquals("There should be one parameter block per setup.", algoSetSize, paramBlockSetSize)

  /** Checks whether the value in each of the given parameter blocks equals the name of a given factory class. */
  def testFactoryClassSetup(factory: Factory, paramBlocks: Seq[ParamBlock]): Unit =
    testFactoryClassSetup(factory.getClass().getName(), paramBlocks, pb => pb.getValue().asInstanceOf[Any])

  /** Checks whether the name of a certain sub-block equals the name of a given factory class (in all given parameter blocks). */
  def testFactoryClassInSubBlocks(factoryClassName: String, paramBlocks: Seq[ParamBlock]): Unit = testFactoryClassSetup(factoryClassName, paramBlocks, pb => {
    assertTrue("Parameter block '" + pb + "' should have a sub-block called '" + factoryClassName + "'.", pb.hasSubBlock(factoryClassName))
    assertNotNull(pb.getSubBlockValue(factoryClassName))
    factoryClassName //make sure assertion in calling function holds
  })

  /** Checks whether some element of the given parameter block equals the name of a given factory class. */
  def testFactoryClassSetup(factoryClassName: String, paramBlocks: Seq[ParamBlock], valueExtractor: ParamBlock => Any): Unit =
    paramBlocks.foreach(pb => assertEquals(factoryClassName, valueExtractor(pb)))
}
