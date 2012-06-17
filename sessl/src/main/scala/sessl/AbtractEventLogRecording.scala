package sessl

/**
 * Trait to create an event log file for each simulation run, which contains each *single* event from the execution.
 *
 * In contrast to {@link sessl.AbstractObservation}, the (potentially huge amount of) data is not held in memory and cannot be further process in SESSL.
 *
 * @author Roland Ewald
 *
 */
abstract trait AbtractEventLogRecording extends ExperimentConfiguration {
  override def configure = {
    super.configure()
    configureEventLogRecording()
  }

  /** Configure event log recording. */
  def configureEventLogRecording(): Unit
}