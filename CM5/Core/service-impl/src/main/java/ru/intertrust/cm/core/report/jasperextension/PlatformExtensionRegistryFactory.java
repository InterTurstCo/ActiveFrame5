package ru.intertrust.cm.core.report.jasperextension;

import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;

public class PlatformExtensionRegistryFactory implements ExtensionsRegistryFactory{

    @Override
    public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {        
        return PlatformExtensionsRegistry.getInstance();            
    }

}
