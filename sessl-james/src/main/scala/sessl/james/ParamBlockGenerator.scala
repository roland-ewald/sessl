package sessl.james

import sessl.Algorithm
import sessl.util.AlgorithmSet
import sessl.util.CreatableFromVariables
import java.util.logging.Level
import james.SimSystem

/**
 * Utility functions to generate parameter blocks.
 * @author Roland Ewald
 *
 */
object ParamBlockGenerator {

  /** Create a set of parameter blocks from sessl algorithms. */
  def createParamBlockSet[T <: JamesIIAlgo[Factory]](algoSet: AlgorithmSet[T]): Seq[ParamBlock] =
    for (algorithm <- algoSet.algorithms) yield createParamBlock(algorithm)

  /** Creates a parameter block from a sessl algorithm specification. */
  def createParamBlock[T <: JamesIIAlgo[Factory]](algorithm: T): ParamBlock = {
    val paramBlock = new ParamBlock(algorithm.factory.getClass.getName)
    algorithm match {
      case a: CreatableFromVariables[_] => a.getInternalParameters().foreach(entry => addToParamBlock(paramBlock, entry))
      case _ => report(Level.INFO, "Algorithm " + algorithm + " cannot be parameterized.")
    }
    paramBlock
  }

  /**
   * Recursively adds sub-blocks to the parameter block.
   * Basically filters out which elements are algorithms in themselves and hence need their own parameters to be set up.
   *
   * @param paramBlock
   *          the parameter block
   * @param nameAndValue
   *          the name and value to be specified
   */
  private[this] def addToParamBlock(paramBlock: ParamBlock, nameAndValue: (String, Any)): Unit = {
    nameAndValue._2 match {
      case algo: JamesIIAlgo[_] =>
        paramBlock.addSubBlock(Registry.getBaseFactoryFor(algo.factory.getClass).getName(), createParamBlock(algo))
      case _ =>
        paramBlock.addSubBlock(nameAndValue._1, nameAndValue._2)
    }

  }

}