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
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

@Stateless(name = "AfterCommitExtensionPointService")
@Local(AfterCommitExtensionPointService.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AfterCommitExtensionPointServiceImpl implements AfterCommitExtensionPointService {
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
        for (DomainObject domainObject : domainObjectsModification.getCreatedDomainObjects()) {
            if (domainObject != null) {
                // Вызов точки расширения после создания после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                for (String parentType : parentTypes) {
                    extensionService.getExtentionPoint(AfterCreateAfterCommitExtentionHandler.class, parentType).onAfterCreate(domainObject);
                }
                extensionService.getExtentionPoint(AfterCreateAfterCommitExtentionHandler.class, "").onAfterCreate(domainObject);
            }
        }

        List<DomainObject> changeStatusDomainObjects = domainObjectDao.find(domainObjectsModification.getChangeStatusDomainObjectIds(), sysAccessTocken);
        for (DomainObject domainObject : changeStatusDomainObjects) {
            if (domainObject != null) {
                // Вызов точки расширения после смены статуса после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                for (String parentType : parentTypes) {
                    extensionService.getExtentionPoint(AfterChangeStatusAfterCommitExtentionHandler.class, parentType).onAfterChangeStatus(domainObject);
                }
                extensionService.getExtentionPoint(AfterChangeStatusAfterCommitExtentionHandler.class, "").onAfterChangeStatus(domainObject);
            }
        }

        for (DomainObject domainObject : domainObjectsModification.getSavedDomainObjects()) {
            if (domainObject != null) {

                // Вызов точки расширения после сохранения после коммита
                String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
                final List<FieldModification> fieldModificationList = domainObjectsModification.getFieldModificationList(domainObject.getId());
                for (String typeName : parentTypes) {
                    extensionService.getExtentionPoint(AfterSaveAfterCommitExtensionHandler.class, typeName).onAfterSave(domainObject, fieldModificationList);
                }
                extensionService.getExtentionPoint(AfterSaveAfterCommitExtensionHandler.class, "").onAfterSave(domainObject, fieldModificationList);
            }
        }

        for (DomainObject deletedDomainObject : domainObjectsModification.getDeletedDomainObjects()) {
            // Вызов точки расширения после удаления после коммита
            String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(deletedDomainObject.getTypeName());
            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(AfterDeleteAfterCommitExtensionHandler.class, typeName).onAfterDelete(deletedDomainObject);
            }
            extensionService.getExtentionPoint(AfterDeleteAfterCommitExtensionHandler.class, "").onAfterDelete(deletedDomainObject);
        }
    }
}
