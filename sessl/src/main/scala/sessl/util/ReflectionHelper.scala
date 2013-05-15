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
import scala.reflect.runtime.{ currentMirror => cm }
import scala.reflect.runtime.universe._

/**
 * Provides some helper methods for reflection.
 *
 * @author Roland Ewald
 */
object ReflectionHelper {

  /**
   * Get parameter names and their default values for all constructor arguments.
   * Assumes that there are default values for each parameter.
   *
   * TODO: This does not work with case classes that are nested within functions
   * 
   * This method needs to be synchronized, otherwise calls to the reflection API may provoke 
   * a <code>scala.reflect.internal.Symbols$CyclicReference</code>.
   *
   * @param a any product (case class instance)
   * @return list of (parameter name, default value) tuples, in correct order
   */
  def caseClassConstrArgs(a: Product): Seq[(String, Any)] = synchronized {

    val classSymbol = cm.classSymbol(a.getClass)

    val moduleInstanceMirror =
      if (classSymbol.isStatic) { //Normal case class...
        val companionObj = classSymbol.companionSymbol.asModule
        cm.reflect(cm.reflectModule(companionObj).instance)
      } else { //... or belongs to an inner type?

        // Get instance mirror for outer type
        val rtMirror = runtimeMirror(a.getClass.getClassLoader)
        val outerType = getOuter(a)
        val instMirrorOuterType = rtMirror.reflect(outerType)

        // Get module mirror of inner class' companion
        val innerClassMirror = instMirrorOuterType.reflectClass(classSymbol)
        val companionSymbol = innerClassMirror.symbol.companionSymbol
        val moduleMirror = instMirrorOuterType.reflectModule(companionSymbol.asModule)

        // Get instance mirror for companion object
        rtMirror.reflect(moduleMirror.instance)
      }

    argsAndDefaults(moduleInstanceMirror, "apply")
  }

  /**
   * Gets outer type of an object. Assumes the parameter is an instance of an inner class.
   *  @param the object
   *  @return the outer type
   */
  def getOuter[T](a: Any) = a.getClass.getField("$outer").get(a)

  /**
   * Given an instance mirror and a method name, return the argument names and their default values.
   * This method requires
   *
   * The code has been derived from this answer at stack overflow: http://stackoverflow.com/a/13813000/109942
   * (answered question http://stackoverflow.com/q/14034142/109942)
   *
   * @param im the instance mirror
   * @param the name of the method to be instantiated
   * @return list of (parameter name, default value) tuples, in correct order
   */
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

  /**
   * Retrieves reference to an object by its name.
   * @param the class name of the object
   * @return the reference to the object
   */
  def objectReferenceByName[A](className: String): A = {
    val rtMirror = runtimeMirror(getClass.getClassLoader)
    val moduleSymbol = rtMirror.staticModule(className)
    val moduleMirror = rtMirror reflectModule moduleSymbol
    moduleMirror.instance.asInstanceOf[A]
  }

}