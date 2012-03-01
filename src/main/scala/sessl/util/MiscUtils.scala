package sessl.util

import james.SimSystem
import java.util.logging.Level

/**
 * Some utilities.
 *
 * @author Roland Ewald
 *
 */
object MiscUtils {

  /** 'Safely' applies some arguments to a function. Any exceptions will be caught and logged.  */
  def saveApply[X, Y](f: X => Y, args: X): Option[Y] = {
    try {
      Some(f.apply(args))
    } catch {
      case ex => println("Application of " + f + " failed."); ex.printStackTrace(); None //TODO: use logging here!
    }
  }

}



