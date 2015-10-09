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
import ru.intertrust.cm.core.model.*;

import java.util.*;

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
        } catch (SystemException | NullPointerException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in exists", ex);
            throw new UnexpectedException("CrudService", "exists", "id:" + id, ex);
        }
    }


    public DomainObject find(Id id) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = null;

            if (isReadPermittedToEverybody(id)) {
                accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
            } else {
                accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.READ);
            }

            DomainObject result = domainObjectDao.find(id, accessToken);
            if (result == null) {
                throw new ObjectNotFoundException(id);
            }

            return result;
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in find", ex);
            throw new UnexpectedException("CrudService", "find", "id:" + id, ex);
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
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in find", ex);
            throw new UnexpectedException("CrudService", "find", "ids:" + Arrays.toString(idsArray), ex);
        }
    }


    public List<DomainObject> findAll(String domainObjectType) {
        try {
            return findAll(domainObjectType, false);
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findAll", ex);
            throw new UnexpectedException("CrudService", "findAll", "domainObjectType:" + domainObjectType, ex);
        }
    }


    public List<DomainObject> findAll(String domainObjectType, boolean exactType) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain Object type can not be null or empty");
        }

        try {
            AccessToken accessToken = null;

            if (isReadPermittedToEverybody(domainObjectType)) {
                accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
            } else {
                String user = currentUserAccessor.getCurrentUser();
                accessToken = accessControlService.createAccessToken(user, null, DomainObjectAccessType.READ);
            }

            return domainObjectDao.findAll(domainObjectType, exactType, accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findAll", ex);
            throw new UnexpectedException("CrudService", "findAll", "domainObjectType:" + domainObjectType, ex);
        }
    }

    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField) {
        try {
            return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, false);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findLinkedDomainObjects", ex);
            throw new UnexpectedException("CrudService", "findLinkedDomainObjects",
                    "domainObjectId:" + domainObjectId + ", linkedType:" + linkedType + ", " + linkedField, ex);
        }
    }


    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,                                                      boolean exactType) {
        try {
            AccessToken accessToken = createAccessTokenForFindLinkedDomainObjects(linkedType);
            return domainObjectDao.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType,
                    accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findLinkedDomainObjects", ex);
            throw new UnexpectedException("CrudService", "findLinkedDomainObjects",
                    "domainObjectId:" + domainObjectId + " linkedType:" + linkedType
                            + " linkedField:" + linkedField, ex);
        }
    }


    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField) {
        try {
            return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, false);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findLinkedDomainObjectsIds", ex);
            throw new UnexpectedException("CrudService", "findLinkedDomainObjectsIds",
                    "domainObjectId:" + domainObjectId + ", linkedType:" + linkedType + ", " + linkedField, ex);
        }
    }


    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType) {
        try {
            AccessToken accessToken = createAccessTokenForFindLinkedDomainObjects(linkedType);
            return domainObjectDao.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType,
                    accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findLinkedDomainObjectsIds", ex);
            throw new UnexpectedException("CrudService", "findLinkedDomainObjectsIds",
                    "domainObjectId:" + domainObjectId + " linkedType:" + linkedType
                            + " linkedField:" + linkedField, ex);
        }
    }

    public String getDomainObjectType(Id id) {
        try {
            return domainObjectTypeIdCache.getName(id);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDomainObjectType", ex);
            throw new UnexpectedException("CrudService", "getDomainObjectType", "id:" + id, ex);
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
        } catch (SystemException e) {
            throw e;
        } catch (Exception ex){
            logger.error("Unexpected exception caught in findByUniqueKey", ex);
            throw new UnexpectedException("CrudService", "findByUniqueKey",
                    "domainObjectType:" + domainObjectType, ex);
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
            List<String> parentTypes = getAllParentTypes(name);
            //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
            parentTypes.add("");
            for (String typeName : parentTypes) {
                AfterCreateExtentionHandler extension = extensionService.getExtentionPoint(AfterCreateExtentionHandler.class, typeName);
                extension.onAfterCreate(domainObject);
            }

            return domainObject;
        } catch (SystemException | NullPointerException | IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in createDomainObject", ex);
            throw new UnexpectedException("CrudService", "createDomainObject", "name:" + name, ex);
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
        } catch (SystemException | IllegalArgumentException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in save", ex);
            throw new UnexpectedException("CrudService", "save", "domainObject:" + domainObject, ex);
        }
    }

    public List<DomainObject> save(List<DomainObject> domainObjects) {
        try {
            for (DomainObject domainObject : domainObjects) {
                checkForAttachment(domainObject.getTypeName());
            }

            AccessToken accessToken = createSystemAccessToken();
            return domainObjectDao.save(domainObjects, accessToken);
        } catch (SystemException | IllegalArgumentException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in save", ex);
            throw new UnexpectedException("CrudService", "save", "domainObjects:" + Arrays.toString(domainObjects.toArray()), ex);
        }
    }

    public DomainObject findAndLock(Id id) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.WRITE);

            DomainObject result = domainObjectDao.findAndLock(id, accessToken);
            if (result == null) {
                throw new ObjectNotFoundException(id);
            }

            return result;
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findAndLock", ex);
            throw new UnexpectedException("CrudService", "findAndLock", "id:" + id, ex);
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
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in delete", ex);
            throw new UnexpectedException("CrudService", "delete", "id:" + id, ex);
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
        } catch (SystemException | NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in delete", ex);
            throw new UnexpectedException("CrudService", "delete", "ids:" + Arrays.toString(ids.toArray()), ex);
        }
    }

    public DomainObject findAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return domainObjectDao.finAndLockByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken);
        } catch (SystemException e) {
            throw e;
        } catch (Exception ex){
            logger.error("Unexpected exception caught in findAndLockByUniqueKey", ex);
            throw new UnexpectedException("CrudService", "findAndLockByUniqueKey",
                    "domainObjectType:" + domainObjectType, ex);
        }
    }

    private AccessToken createAccessTokenForFindLinkedDomainObjects(String linkedType) {
        AccessToken accessToken = null;
        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, linkedType);

        if (isReadPermittedToEverybody(linkedType) || isAdministratorWithAllPermissions) {
            accessToken = accessControlService.createSystemAccessToken("TransactionalCrudServiceImpl");
        } else {
            String user = currentUserAccessor.getCurrentUser();
            accessToken = accessControlService.createCollectionAccessToken(user);
        }
        return accessToken;
    }

    /**
     * Получение всей цепочки родительских типов начиная от переданноготв параметре
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
