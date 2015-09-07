package ru.intertrust.cm.core.dao.impl.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
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

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;

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
    public void afterCommit(DomainObjectsModification domainObjectsModification) {
        AccessToken sysAccessTocken = accessControlService.createSystemAccessToken(getClass().getName());

        for (DomainObject domainObject : domainObjectsModification.getCreatedDomainObjects()) {
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

        List<DomainObject> changeStatusDomainObjects =
                domainObjectDao.find(domainObjectsModification.getChangeStatusDomainObjectIds(), sysAccessTocken);
        for (DomainObject domainObject : changeStatusDomainObjects) {
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

        for (DomainObject domainObject : domainObjectsModification.getSavedDomainObjects()) {
            if (domainObject != null) {

                // Вызов точки расширения после сохранения после коммита
                List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
                //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
                parentTypes.add("");
                for (String typeName : parentTypes) {
                    AfterSaveAfterCommitExtensionHandler extension = extensionService
                            .getExtentionPoint(AfterSaveAfterCommitExtensionHandler.class, typeName);
                    extension.onAfterSave(domainObject,
                            domainObjectsModification.getFieldModificationList(domainObject.getId()));
                }
            }
        }

        for (DomainObject deletedDomainObject : domainObjectsModification.getDeletedDomainObjects()) {
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
}
