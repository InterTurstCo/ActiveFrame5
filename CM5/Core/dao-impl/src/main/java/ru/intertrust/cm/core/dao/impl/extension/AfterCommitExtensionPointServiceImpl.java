package ru.intertrust.cm.core.dao.impl.extension;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "AfterCommitExtensionPointService")
@Local(AfterCommitExtensionPointService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AfterCommitExtensionPointServiceImpl implements AfterCommitExtensionPointService {

	private static final Logger logger = LoggerFactory.getLogger(AfterCommitExtensionPointServiceImpl.class);

    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ExtensionService extensionService;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @EJB
    private AfterCommitExtensionPointService newTransactionService;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void afterCommit(DomainObjectsModification domainObjectsModification) {
        List<DomainObject> createdDomainObjects = domainObjectsModification.getCreatedDomainObjects();
        List<Id> changeStatusDomainObjectIds = domainObjectsModification.getChangeStatusDomainObjectIds();
        Collection<DomainObject> savedDomainObjects = domainObjectsModification.getSavedDomainObjects();
        Collection<DomainObject> deletedDomainObjects = domainObjectsModification.getDeletedDomainObjects();
        if (createdDomainObjects.isEmpty() && changeStatusDomainObjectIds.isEmpty() && savedDomainObjects.isEmpty() && deletedDomainObjects.isEmpty()) {
            return;
        }
        newTransactionService.performAfterCommit(domainObjectsModification);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performAfterCommit(DomainObjectsModification domainObjectsModification) {
        AccessToken sysAccessTocken = accessControlService.createSystemAccessToken(getClass().getName());
        logger.debug("performAfterCommit: call AfterCreateAfterCommit ext points / begin");
        for (DomainObject domainObject : domainObjectsModification.getCreatedDomainObjects()) {
            if (domainObject != null) {
                // Вызов точки расширения после создания после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                for (String parentType : parentTypes) {
                	final AfterCreateAfterCommitExtentionHandler handler = extensionService.getExtensionPoint(AfterCreateAfterCommitExtentionHandler.class, parentType);
                	logger.debug("performAfterCommit: Call AfterCreateAfterCommit ext point {} for parentType = {} / begin", handler, parentType);
                    handler.onAfterCreate(domainObject);
                    logger.debug("performAfterCommit: Call AfterCreateAfterCommit ext point {} for parentType = {} / end", handler, parentType);
				}

                final AfterCreateAfterCommitExtentionHandler handler = extensionService.getExtensionPoint(AfterCreateAfterCommitExtentionHandler.class, "");
            	logger.debug("performAfterCommit: Call AfterCreateAfterCommit ext point {} (default) / begin", handler);
            	handler.onAfterCreate(domainObject);
                logger.debug("performAfterCommit: Call AfterCreateAfterCommit ext point {} (default) / end", handler);
            }
        }
        logger.debug("performAfterCommit: call AfterCreateAfterCommit ext points / end");

        logger.debug("performAfterCommit: call AfterChangeStatusAfterCommit ext points / begin");
        List<DomainObject> changeStatusDomainObjects = domainObjectDao.find(domainObjectsModification.getChangeStatusDomainObjectIds(), sysAccessTocken);
        for (DomainObject domainObject : changeStatusDomainObjects) {
            if (domainObject != null) {
                // Вызов точки расширения после смены статуса после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                for (String parentType : parentTypes) {
                	final AfterChangeStatusAfterCommitExtentionHandler handler = extensionService.getExtensionPoint(AfterChangeStatusAfterCommitExtentionHandler.class, parentType);
                	logger.debug("performAfterCommit: Call AfterChangeStatusAfterCommit ext point {} for parentType = {} / begin", handler, parentType);
                    handler.onAfterChangeStatus(domainObject);
                    logger.debug("performAfterCommit: Call AfterChangeStatusAfterCommit ext point {} for parentType = {} / end", handler, parentType);
				}

                final AfterChangeStatusAfterCommitExtentionHandler handler = extensionService.getExtensionPoint(AfterChangeStatusAfterCommitExtentionHandler.class, "");
                logger.debug("performAfterCommit: Call AfterChangeStatusAfterCommit ext point {} (default) / begin", handler);
                handler.onAfterChangeStatus(domainObject);
                logger.debug("performAfterCommit: Call AfterChangeStatusAfterCommit ext point {} (default) / begin", handler);
            }
        }
        logger.debug("performAfterCommit: call AfterChangeStatusAfterCommit ext points / end");

		logger.debug("performAfterCommit: call AfterSaveAfterCommit ext points / begin");
        for (DomainObject domainObject : domainObjectsModification.getSavedDomainObjects()) {
            if (domainObject != null) {

                // Вызов точки расширения после сохранения после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                final List<FieldModification> fieldModificationList = domainObjectsModification.getFieldModificationList(domainObject.getId());
                for (String typeName : parentTypes) {
                	final AfterSaveAfterCommitExtensionHandler handler = extensionService.getExtensionPoint(AfterSaveAfterCommitExtensionHandler.class, typeName);
                	logger.debug("performAfterCommit: Call AfterSaveAfterCommit ext point {} for parentType = {} / begin", handler, typeName);
                    handler.onAfterSave(domainObject, fieldModificationList);
                    logger.debug("performAfterCommit: Call AfterSaveAfterCommit ext point {} for parentType = {} / end", handler, typeName);
				}

                final AfterSaveAfterCommitExtensionHandler handler = extensionService.getExtensionPoint(AfterSaveAfterCommitExtensionHandler.class, "");
                logger.debug("performAfterCommit: Call AfterSaveAfterCommit ext point {} (default) / begin", handler);
                handler.onAfterSave(domainObject, fieldModificationList);
                logger.debug("performAfterCommit: Call AfterSaveAfterCommit ext point {} (default) / begin", handler);
            }
        }
        logger.debug("performAfterCommit: call AfterSaveAfterCommit ext points / end");

        logger.debug("performAfterCommit: call AfterDeleteAfterCommit ext points / begin");
        for (DomainObject deletedDomainObject : domainObjectsModification.getDeletedDomainObjects()) {
            // Вызов точки расширения после удаления после коммита
            String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(deletedDomainObject.getTypeName());
            for (String typeName : parentTypes) {
            	final AfterDeleteAfterCommitExtensionHandler handler = extensionService.getExtensionPoint(AfterDeleteAfterCommitExtensionHandler.class, typeName);
            	logger.debug("performAfterCommit: Call AfterDeleteAfterCommit ext point {} for parentType = {} / begin", handler, typeName);
            	handler.onAfterDelete(deletedDomainObject);
            	logger.debug("performAfterCommit: Call AfterDeleteAfterCommit ext point {} for parentType = {} / end", handler, typeName);
			}

            final AfterDeleteAfterCommitExtensionHandler handler = extensionService.getExtensionPoint(AfterDeleteAfterCommitExtensionHandler.class, "");
            logger.debug("performAfterCommit: Call AfterDeleteAfterCommit ext point {} (default) / begin", handler);
            handler.onAfterDelete(deletedDomainObject);
            logger.debug("performAfterCommit: Call AfterDeleteAfterCommit ext point {} (default) / begin", handler);
        }
        logger.debug("performAfterCommit: call AfterDeleteAfterCommit ext points / end");
    }
}
