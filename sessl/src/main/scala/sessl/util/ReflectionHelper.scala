/**
 * *****************************************************************************
 * Copyright 2012-2013 Roland Ewald
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
 * ****************************************************************************
 */
package sessl.util

import scala.reflect._
import scala.reflect.runtime.{currentMirror => cm}
import scala.reflect.runtime.universe._

/**
 * Provides some helper methods for reflection.
 *
 * Some of the code has been derived from this answer at stack overflow: http://stackoverflow.com/a/13813000/109942 
 * which answers this question: http://stackoverflow.com/q/14034142/109942
 *
 * @author Roland Ewald
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