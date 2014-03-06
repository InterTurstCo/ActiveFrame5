package ru.intertrust.cm.core.business.impl;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.load.ImportData;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

@Stateless(name = "ImportDataService")
@Local(ImportDataService.class)
@Remote(ImportDataService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ImportDataServiceImpl implements ImportDataService {

    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AttachmentContentDao attachmentContentDao;
    @Resource
    private EJBContext context;

    @Override
    public void importData(byte[] importFileAsByteArray) {
        importData(importFileAsByteArray, null);
    }

    @Override
    public void importData(byte[] importFileAsByteArray, String encoding) {
       importData(importFileAsByteArray, encoding, false);
    }

	@Override
	public void importData(byte[] importFileAsByteArray, String encoding,
			boolean rewrite) {
		 try {
	            ImportData ImportData =
	                    new ImportData(collectionsDao, configurationExplorer, domainObjectDao, accessControlService,
	                            attachmentContentDao, context.getCallerPrincipal().getName());
	            ImportData.importData(importFileAsByteArray, encoding, rewrite);
	        } catch (Exception ex) {
	            throw new FatalException("Error load data", ex);
	        }
		
	}
}
