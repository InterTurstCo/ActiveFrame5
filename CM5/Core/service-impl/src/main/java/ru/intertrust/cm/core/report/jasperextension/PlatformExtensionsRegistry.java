package ru.intertrust.cm.core.report.jasperextension;

import java.util.Collections;
import java.util.List;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.repo.RepositoryService;

public class PlatformExtensionsRegistry implements ExtensionsRegistry {

    private static final PlatformExtensionsRegistry INSTANCE =  new PlatformExtensionsRegistry();
    
    public static PlatformExtensionsRegistry getInstance()
    {
        return INSTANCE;
    }    
    
    @Override
    public <T> List<T> getExtensions(Class<T> extensionType) {
        if (RepositoryService.class.equals(extensionType))
        {
            return (List<T>) Collections.singletonList(new PlatformRepositoryService(DefaultJasperReportsContext.getInstance()));
        }
        return null;
    }
}
