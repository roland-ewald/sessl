package sessl.sbw.util

import edu.caltech.sbw.SBW

/**
 * @author Roland Ewald
 */
class SBWModuleDirectory {

  val moduleInstances = SBW.getExistingModuleInstances()

  val moduleDescriptors = {
    for (m <- moduleInstances if !m.getDescriptor().getName().contains("anonymous-"))
      yield (m.getDescriptor().getName(), m.getDescriptor())
  }.toMap

  for (md <- moduleDescriptors.values) {
    println()
    println(md.getName())
    println("==========")
    println(md.getCommandLine())
    println(md.getDisplayName())
    println(md.getHelp())
    println(md.getManagementType())
    for (sd <- md.getServiceDescriptors()) {
      println("\t" + sd.getDisplayName())
    }
  }

}
