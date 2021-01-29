package ru.intertrust.cm.core.report.jasperextension;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.RepositoryContext;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;
import ru.intertrust.cm.core.business.api.ResourceService;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PlatformRepositoryService implements RepositoryService{

    private JasperReportsContext jasperReportsContext;

    public PlatformRepositoryService(JasperReportsContext jasperReportsContext) 
    {
        this.jasperReportsContext = jasperReportsContext;
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
        // todo: when running report we access its datasource inside transaction (Profile Service is ejb)
        CurrentDataSourceContext currentDataSourceContext = SpringApplicationContext.getContext().getBean(CurrentDataSourceContext.class);
        final String originalDatasource = currentDataSourceContext.get();
        currentDataSourceContext.setToMaster(); // read from MASTER to avoid "cannot execute PREPARE TRANSACTION during recovery exception" on transaction commit (EJB opens it)

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
        currentDataSourceContext.set(originalDatasource); // restore after switching to MASTER
        return result;
    }
}
