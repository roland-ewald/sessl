package sessl.util

/** Provides helper functions for Java<->Scala interaction.
 *  @author Roland EWald
 *
 */
object JavaToScala {

  def toScala[K, V](input: java.util.Map[K, V]): Map[K, V] = {
    var output = Map[K, V]()
    val it = input.entrySet().iterator()
    while (it.hasNext()) {
      val entry = it.next()
      output += ((entry.getKey(), entry.getValue()))
    }
    output
  }

}