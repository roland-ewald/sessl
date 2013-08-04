package sessl.sbw.util

import edu.caltech.sbw.SBW
import edu.caltech.sbw.ServiceDescriptor
import edu.caltech.sbw.ModuleDescriptor

/**
 * @author Roland Ewald
 */
class SBWModuleDirectory {

  val anonModulePrefix = "anonymous-"

  val moduleDescriptors = {
    for (md <- SBW.getModuleDescriptors(true) if !md.getName().contains(anonModulePrefix))
      yield (md.getName(), md)
  }.toMap

  for (md <- moduleDescriptors.values) {
    println {
      s"""
      
      Name: ${md.getName}
      =====================
      - Command line: ${md.getCommandLine}
      - Display name: ${md.getDisplayName}
      - Help note: ${md.getHelp}
      - Management type: ${md.getManagementType}
      - Services:
      ${servicesOfModule(md)}
      """
    }
  }

  def servicesOfModule(md: ModuleDescriptor): String = {
    (for (sd <- md.getServiceDescriptors) yield {
      s"""
        \tName: ${sd.getName}
        \tDisplay name: ${sd.getDisplayName}
        \tCategory: ${sd.getCategory}
        \tHelp note: ${sd.getHelp}
        \tMethods: ${methodsOfService(sd)}
        """
    }).foldLeft("")(_ + _)
  }

  def methodsOfService(sd: ServiceDescriptor): String = {
	val service = sd.getServiceInModuleInstance
	(for (m <- service.getMethods) yield {
	  //TODO
	}).foldLeft("")(_ + _)
  }

}
