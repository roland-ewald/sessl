package sessl

import scala.collection.mutable.ListBuffer
import sessl.util.MiscUtils
import sessl.util.ExperimentObserver

/** Super class for all fundamental experiment classes.
 *  It handles the execution of the event handlers and checks whether other traits that may be mixed in by the user have properly called their super methods.
 *  @author Roland Ewald
 */
abstract class AbstractExperiment extends BasicExperimentConfiguration with SupportModelConfiguration
  with SupportSimulatorConfiguration with SupportRNGSetup with SupportReplicationConditions with SupportStoppingConditions {

  /** This flag checks whether the stacked configuration traits have properly called their super methods.
   *  This is possible because the experiment object itself is at the end of this line.
   */
  private[this] var configureCalled = false

  /** Abstract method to create the basic setup (as configure is already used for checking whether the traits properly called super.configure()).
   *  In this function, the experiment should be initialized to conform to all elements provided in this class.
   */
  protected def basicConfiguration(): Unit

  /** Called to execute the experiment. */
  protected def execute(): Unit

  /** Setting the flags that control a proper call hierarchy, calling event handlers if installed. */
  override def configure() = { configureCalled = true }

  /** Prepares the experiment for execution. */
  private final def prepare() = {
    basicConfiguration()
    configure()
    require(configureCalled, "Configuration incomplete: one of the mixed-in traits does not implement stacking properly (i.e., super.configure() is not called!).")
  }

}

/** Methods that operate on the specified experiments. */
object AbstractExperiment {

  /** Execute experiments sequentially.
   *
   *  @param experiments
   *            the experiments
   */
  def execute(exps: AbstractExperiment*) = for (exp <- exps) {
    exp.prepare()
    exp.execute()
    require(exp.isDone, "The experiment seems to have finished incomplete.")
  }

  //Define additional methods for doing things with experiments here...
}