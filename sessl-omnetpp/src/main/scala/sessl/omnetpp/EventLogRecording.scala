package sessl.omnetpp

import sessl.ExperimentConfiguration
import sessl.AbtractEventLogRecording

/**
 * Records a detailed event log for each run.
 *
 * @author Roland Ewald
 */
trait EventLogRecording extends AbtractEventLogRecording {
  this: Experiment =>

  override def configureEventLogRecording() = {
    writeComment("Event Log Recording")
    write("record-eventlog", "true")
  }

}