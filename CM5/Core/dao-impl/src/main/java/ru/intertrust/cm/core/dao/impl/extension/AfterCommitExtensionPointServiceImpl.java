package ru.intertrust.cm.core.dao.impl.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;

@Stateless(name = "AfterCommitExtensionPointService")
@Local(AfterCommitExtensionPointService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AfterCommitExtensionPointServiceImpl implements AfterCommitExtensionPointService {
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ExtensionService extensionService;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public void afterCommit(Map<Id, Map<String, FieldModification>> savedDomainObjects, List<Id> createdDomainObjects,
            Map<Id, DomainObject> deletedDomainObjects, List<Id> changeStatusDomainObjects) {
        AccessToken sysAccessTocken = accessControlService.createSystemAccessToken(getClass().getName());

        for (Id createdId : createdDomainObjects) {
            DomainObject domainObject = domainObjectDao.find(createdId, sysAccessTocken);
            if (domainObject != null) {
                // Вызов точки расширения после создания после коммита
                List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
                //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
                parentTypes.add("");
                for (String typeName : parentTypes) {
                    AfterCreateAfterCommitExtentionHandler extension = extensionService
                            .getExtentionPoint(AfterCreateAfterCommitExtentionHandler.class, typeName);
                    extension.onAfterCreate(domainObject);
                }
            }
        }

        for (Id id : changeStatusDomainObjects) {
            DomainObject domainObject = domainObjectDao.find(id, sysAccessTocken);

            if (domainObject != null) {

                // Вызов точки расширения после смены статуса после коммита
                List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
                //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
                parentTypes.add("");
                for (String typeName : parentTypes) {
                    AfterChangeStatusAfterCommitExtentionHandler extension = extensionService
                            .getExtentionPoint(AfterChangeStatusAfterCommitExtentionHandler.class, typeName);
                    extension.onAfterChangeStatus(domainObject);
                }
            }
        }

        for (Id id : savedDomainObjects.keySet()) {
            DomainObject domainObject = domainObjectDao.find(id, sysAccessTocken);
            if (domainObject != null) {

                // Вызов точки расширения после сохранения после коммита
                List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
                //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
                parentTypes.add("");
                for (String typeName : parentTypes) {
                    AfterSaveAfterCommitExtensionHandler extension = extensionService
                            .getExtentionPoint(AfterSaveAfterCommitExtensionHandler.class, typeName);
                    extension.onAfterSave(domainObject, getFieldModificationList(savedDomainObjects.get(id)));
                }
            }
        }

        for (DomainObject deletedDomainObject : deletedDomainObjects.values()) {
            // Вызов точки расширения после удаления после коммита
            List<String> parentTypes = getAllParentTypes(deletedDomainObject.getTypeName());
            //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
            parentTypes.add("");
            for (String typeName : parentTypes) {
                AfterDeleteAfterCommitExtensionHandler extension = extensionService
                        .getExtentionPoint(AfterDeleteAfterCommitExtensionHandler.class, typeName);
                extension.onAfterDelete(deletedDomainObject);
            }
        }
    }

    /**
     * Получение всей цепочки родительских типов начиная от переданноготв
     * параметре
     * @param name
     * @return
     */
    private List<String> getAllParentTypes(String name) {
        List<String> result = new ArrayList<String>();
        result.add(name);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, name);
        if (domainObjectTypeConfig.getExtendsAttribute() != null) {
            result.addAll(getAllParentTypes(domainObjectTypeConfig.getExtendsAttribute()));
        }

        return result;
    }

    private List<FieldModification> getFieldModificationList(Map<String, FieldModification> map) {
        List<FieldModification> result = new ArrayList<FieldModification>();
        for (FieldModification field : map.values()) {
            result.add(field);
        }
        return result;
    }
}
