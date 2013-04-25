package sessl.util

import scala.reflect._
import scala.reflect.runtime.{currentMirror => cm}
import scala.reflect.runtime.universe._

/**
 *
 * Question:
 * http://stackoverflow.com/q/14034142/109942
 *
 * Answer adapted from:
 * http://stackoverflow.com/a/13813000/109942
 *
 * @author roland
 */
object ReflectionHelper {

  def caseClassConstrArgs[A](a: A): Seq[(String, Any)] = {
    caseClassConstrInfo(cm.classSymbol(a.getClass))
  }

  def caseClassConstrInfo(c: ClassSymbol): Seq[(String, Any)] = {
    val companionObj = c.companionSymbol.asModule
    argsAndDefaults(cm.reflect(cm.reflectModule(companionObj).instance), "apply")
  }

  def argsAndDefaults(im: InstanceMirror, name: String): Seq[(String, Any)] = {
    val ts = im.symbol.typeSignature
    val method = ts.member(newTermName(name)).asMethod

    def valueForParam(param: (Symbol, Int)): (String, Any) = {
      val defaultFunc = ts.member(newTermName(s"$name$$default$$${param._2 + 1}"))
      require(defaultFunc != NoSymbol)
      (param._1.name.toString(), im.reflectMethod(defaultFunc.asMethod)())
    }

    (for (ps <- method.paramss; p <- ps) yield p).zipWithIndex.map(valueForParam)
  }

}