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
package sessl.util

import java.io.File

/** Some utilities.
 *  @author Roland Ewald
 */
object MiscUtils extends Logging {

  /** 'Safely' applies some arguments to a function. Any exceptions will be caught and logged.
   *  @param f the function to be executed
   */
  def saveApply[X, Y](f: X => Y, args: X): Option[Y] = {
    try {
      Some(f.apply(args))
    } catch {
      case ex:Throwable => logger.warn("Application of " + f + " failed.", ex); None
    }
  }

  /** Types are equal.
   *
   *  @param values
   *            the values
   *  @param reactOnInEquality
   *            the react on in equality
   *  @return true, if successful
   */
  def typesAreEqual(values: Seq[Any])(reactOnInEquality: Any => Unit): Boolean = {
    val nonCompliantValues = filterByEqualType(values.head.getClass, values)
    nonCompliantValues.foreach(reactOnInEquality)
    nonCompliantValues.isEmpty
  }

  /** Checks whether types of elements in the sequence are equal to the given type.
   *
   *  @param clazz
   *          the type
   *  @param values
   *          the values
   *  @return true, if all elements in the sequence are of this kind
   */
  def typesAreEqual(clazz: java.lang.Class[_], values: Seq[Any]): Boolean =
    filterByEqualType(clazz, values).isEmpty

  /** Checks whether types of elements in the sequence conform to the given type (ie, have the same type or a sub-type).
   *
   *  @param clazz
   *          the type
   *  @param values
   *          the values
   *  @return true, if all elements in the sequence are of this kind
   */
  def typesConform(clazz: java.lang.Class[_], values: Iterable[Any]): Boolean =
    filterByConformantType(clazz, values).isEmpty

  /** Filters a list by the type of its elements.
   *
   *  @param clazz
   *          the type
   *  @param values
   *          the values
   *  @return the sequence of elements of another types
   */
  def filterByEqualType(clazz: java.lang.Class[_], values: Seq[Any]) = values.filter(_.getClass != clazz)

  /** Filter by conformant types (all elements need to be assignable to the given type).
   *
   *  @param clazz
   *          the type
   *  @param values
   *          the values
   *  @return the sequence of elements with non-conforming types
   */
  def filterByConformantType(clazz: java.lang.Class[_], values: Iterable[Any]) = values.filter(x => !clazz.isAssignableFrom(x.getClass))

  /** Gets the value or, if it is empty, gets default instead.
   *
   *  @param value
   *          the value
   *  @param default the default value
   *  @return either the (non-empty) value, or the default
   */
  def getOrEmpty(value: String, default: String) = if (value.isEmpty()) default else value

  /** Deletes file (and all sub-directories and their files, in case it is a directory) recursively.
   *
   *  @param f the file to be deleted
   *  @return true, if deletion of all files was successful
   */
  def deleteRecursively(f: File): Boolean = if (!f.isDirectory) f.delete else {
    f.listFiles().foreach(subFile => deleteRecursively(subFile))
    f.delete
  }

  /** Deletes a file/directory recursively.
   *
   *  @param s the name of the file
   *  @return true, if deletion of all files was successful
   */
  def deleteRecursively(s: String): Boolean = deleteRecursively(new File(s))
}



