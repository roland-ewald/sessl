package sessl.util

import java.util.ArrayList
import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach

object ScalaToJava {

  /** Creates an array list of java Double objects.*/
  def toDoubleList(values: Iterable[Double]): ArrayList[java.lang.Double] = {
    val rv = new ArrayList[java.lang.Double]()
    values.foreach(v => rv.add(v))
    rv
  }

  /** Creates an array list of java Integer objects.*/
  def toIntegerList(values: Iterable[Int]): ArrayList[java.lang.Integer] = {
    val rv = new ArrayList[java.lang.Integer]()
    values.foreach(v => rv.add(v))
    rv
  }

  /** Creates an array list.*/
  def toList[X](values: Iterable[X]): ArrayList[X] = {
    val rv = new ArrayList[X]()
    values.foreach(v => rv.add(v))
    rv
  }

  /** Convert list to double array. */
  def toDoubleArray(values: List[Double]): Array[java.lang.Double] = values.map(_.asInstanceOf[java.lang.Double]).toArray

  /** Convert list to (transposed) nested array. */
  def to2DTransposedJavaStringArray(valueLists: List[String]*): Array[Array[java.lang.String]] = {
    if (valueLists.length == 0)
      return Array.ofDim(0)

    val rv: Array[Array[java.lang.String]] = Array.ofDim(valueLists.head.length)
    for (i <- valueLists.head.indices)
      rv(i) = Array.ofDim(valueLists.length)
    for (i <- valueLists.head.indices; j <- valueLists.indices)
      rv(i)(j) = valueLists(j)(i)
    rv
  }

  /** Convert list to nested array. */
  def to2DJavaDoubleArray(valueLists: List[Double]*) = {
    val rv: Array[Array[java.lang.Double]] = Array.ofDim(valueLists.length)
    for (i <- valueLists.indices)
      rv(i) = toDoubleArray(valueLists(i))
    rv
  }

}