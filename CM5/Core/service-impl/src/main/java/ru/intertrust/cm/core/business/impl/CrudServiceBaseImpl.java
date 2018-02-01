package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateExtentionHandler;
import ru.intertrust.cm.core.model.CrudException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.model.RemoteSuitableException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса для работы c базовы CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 */
public class CrudServiceBaseImpl implements CrudServiceDelegate, CrudServiceDelegate.Remote {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(CrudServiceBaseImpl.class);

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Autowired
    protected UserGroupGlobalCache userGroupCache;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private ExtensionService extensionService;


    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }


    public boolean exists(Id id) {
        try {
            return domainObjectDao.exists(id);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public DomainObject find(Id id) {
        try {
            final AccessToken accessToken;

            if (isReadPermittedToEverybody(id)) {
                accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
            } else {
                String user = currentUserAccessor.getCurrentUser();
                accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.READ);
            }

            DomainObject result = domainObjectDao.find(id, accessToken);
            if (result == null) {
                throw new ObjectNotFoundException(id);
            }

            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<DomainObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("Ids list can not be empty");
        }
        Id[] idsArray = ids.toArray(new Id[ids.size()]);
        try {
            String user = currentUserAccessor.getCurrentUser();

            AccessToken accessToken =
                    accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.READ, false);

            return domainObjectDao.find(ids, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<DomainObject> findAll(String domainObjectType) {
        try {
            return findAll(domainObjectType, false);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<DomainObject> findAll(String domainObjectType, boolean exactType) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain Object type can not be null or empty");
        }

        try {
            AccessToken accessToken;

            if (isReadPermittedToEverybody(domainObjectType)) {
                accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
            } else {
                String user = currentUserAccessor.getCurrentUser();
                accessToken = accessControlService.createAccessToken(user, null, DomainObjectAccessType.READ);
            }

            return domainObjectDao.findAll(domainObjectType, exactType, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField) {
        try {
            return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, false);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,                                                      boolean exactType) {
        try {
            AccessToken accessToken = createAccessTokenForFindLinkedDomainObjects(linkedType);
            return domainObjectDao.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType,
                    accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField) {
        try {
            return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, false);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType) {
        try {
            AccessToken accessToken = createAccessTokenForFindLinkedDomainObjects(linkedType);
            return domainObjectDao.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType,
                    accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public String getDomainObjectType(Id id) {
        try {
            return domainObjectTypeIdCache.getName(id);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }


    public DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken;
            if (isReadPermittedToEverybody(domainObjectType)) {
                accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
            } else {
                accessToken = accessControlService.createCollectionAccessToken(user);
            }
            return domainObjectDao.findByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public IdentifiableObject createIdentifiableObject() {
        // TODO Auto-generated method stub
        return null;
    }

    public DomainObject createDomainObject(String name) {
        try {
            GenericDomainObject domainObject = new GenericDomainObject();
            domainObject.setTypeName(name);

            DomainObjectTypeConfig config = configurationExplorer.getDomainObjectTypeConfig(name);
            if (config == null) {
                throw new IllegalArgumentException("Domain Object Type '" + name + "' doesn't exist");
            }

            for (FieldConfig fieldConfig : config.getSystemFieldConfigs()) {
                domainObject.setValue(fieldConfig.getName(), null);
            }
            for (FieldConfig fieldConfig : config.getFieldConfigs()) {
                domainObject.setValue(fieldConfig.getName(), null);
            }

            Date currentDate = new Date();
            domainObject.setCreatedDate(currentDate);
            domainObject.setModifiedDate(currentDate);

            //Точка расширения после создания
            String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(name);
            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(AfterCreateExtentionHandler.class, typeName).onAfterCreate(domainObject);
            }
            //вызываем обработчики с неуказанным фильтром
            extensionService.getExtentionPoint(AfterCreateExtentionHandler.class, "").onAfterCreate(domainObject);

            return domainObject;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public DomainObject save(DomainObject domainObject) {
        try {
            String domainObjectType = domainObject.getTypeName();
            checkForAttachment(domainObjectType);
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = null;
            if (!domainObject.isNew()) {
                Id objectId = ((GenericDomainObject) domainObject).getId();
                accessToken = accessControlService.createAccessToken(user, objectId, DomainObjectAccessType.WRITE);
            } else {
                accessToken = accessControlService.createDomainObjectCreateToken(user, domainObject);
            }

            DomainObject result = domainObjectDao.save(domainObject, accessToken);
            if (result == null) {
                throw new ObjectNotFoundException(domainObject.getId());
            }

            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public List<DomainObject> save(List<DomainObject> domainObjects) {
        try {
            for (DomainObject domainObject : domainObjects) {
                checkForAttachment(domainObject.getTypeName());
            }

            AccessToken accessToken = createSystemAccessToken();
            return domainObjectDao.save(domainObjects, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public DomainObject findAndLock(Id id) {
        try {
            final String user = currentUserAccessor.getCurrentUser();
            final AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.WRITE);

            DomainObject result = domainObjectDao.findAndLock(id, accessToken);
            if (result == null) {
                throw new ObjectNotFoundException(id);
            }

            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public void delete(Id id) {
        try {
            String objectName = domainObjectTypeIdCache.getName(id);
            checkForAttachment(objectName);
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = null;
            accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.DELETE);
            domainObjectDao.delete(id, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public int delete(List<Id> ids) {
        try {
            if (ids == null || ids.size() == 0) {
                return 0;
            }
            for (Id id : ids) {
                String objectName = domainObjectTypeIdCache.getName(id);
                checkForAttachment(objectName);
            }

            Id[] idsArray = ids.toArray(new Id[ids.size()]);
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.DELETE, false);
            return domainObjectDao.delete(ids, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public DomainObject findAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return domainObjectDao.finAndLockByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private AccessToken createAccessTokenForFindLinkedDomainObjects(String linkedType) {
        AccessToken accessToken;
        Id personId = currentUserAccessor.getCurrentUserId();

        if (isReadPermittedToEverybody(linkedType) || isAdministratorWithAllPermissions(personId, linkedType)) {
            accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
        } else {
            String user = currentUserAccessor.getCurrentUser();
            accessToken = accessControlService.createCollectionAccessToken(user);
        }
        return accessToken;
    }

    private void checkForAttachment(String objectType) {
        if (configurationExplorer.isAttachmentType(objectType)){
            throw new CrudException("Working with Attachments allowed only through AttachmentService");
        }
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("CrudService");
    }

    private boolean isReadPermittedToEverybody(Id id) {
        String domainObjectType = domainObjectTypeIdCache.getName(id);
        return isReadPermittedToEverybody(domainObjectType);
    }

    private boolean isReadPermittedToEverybody(String domainObjectType) {
        domainObjectType = getRelevantType(domainObjectType);
        return configurationExplorer.isReadPermittedToEverybody(domainObjectType);
    }

    private String getRelevantType(String typeName) {
        if (configurationExplorer.isAuditLogType(typeName)) {
            typeName = typeName.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        }
        return typeName;
    }

    private boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        if (personId == null) {
            return false;
        }
        return userGroupCache.isAdministrator(personId) && configurationExplorer.getAccessMatrixByObjectType(domainObjectType) == null;

    }

}
