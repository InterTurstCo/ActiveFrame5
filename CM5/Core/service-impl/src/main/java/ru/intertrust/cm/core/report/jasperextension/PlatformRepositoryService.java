package ru.intertrust.cm.core.report.jasperextension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.RepositoryContext;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;
import ru.intertrust.cm.core.business.api.ResourceService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public class PlatformRepositoryService implements RepositoryService{

    private JasperReportsContext jasperReportsContext;

    @Override
    public void setContext(RepositoryContext context) {
    }
    
    public PlatformRepositoryService(JasperReportsContext jasperReportsContext) 
    {
        this.jasperReportsContext = jasperReportsContext;
    }

    @Override
    public void revertContext() {
    }

    @Override
    public InputStream getInputStream(String uri) {
        return null;
    }

    @Override
    public Resource getResource(String uri) {
        return null;
    }

    @Override
    public void saveResource(String uri, Resource resource) {
    }

    @Override
    public <K extends Resource> K getResource(String uri, Class<K> resourceType) {
        K result = null;
        //Получаем ресурс из сервиса. Проверяем что это наш ресурс
        if (uri.startsWith("res:/")){
            String resName = uri.substring(5);
            //Получаем сам ресурс из сервиса
            ResourceService resourceService = SpringApplicationContext.getContext().getBean(ResourceService.class);
            byte[] blob = resourceService.getBlob(resName);
            if (blob == null){
                throw new FatalException("Resource " + resName + " not found.");
            }
            //Оборачиваем его для jasperReports
            result = (K)new InputStreamResource();
            result.setName(resName);
            ((InputStreamResource)result).setInputStream(new ByteArrayInputStream(blob));            
        }
        
        return result;
    }
}
