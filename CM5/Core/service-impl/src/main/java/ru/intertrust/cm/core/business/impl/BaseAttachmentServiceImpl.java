package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import javax.annotation.Nonnull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.RemoteSuitableException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by andrey on 25.04.14.
 */
public abstract class BaseAttachmentServiceImpl implements BaseAttachmentService {
  
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(RemoteAttachmentServiceImpl.class);

    private static final String FIELD_ATTACH_NAME = "name";

    @Autowired
    private AttachmentContentDao attachmentContentDao;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private CrudService crudService;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserGroupGlobalCache userGroupCache;

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    @Override
    public DomainObject createAttachmentDomainObjectFor(Id objectId, String attachmentType) {
        try {
            GenericDomainObject attachmentDomainObject = (GenericDomainObject) crudService.createDomainObject(attachmentType);

            String domainObjectType = domainObjectTypeIdCache.getName(objectId);

            String attachmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

            attachmentDomainObject.setReference(attachmentLinkedField, objectId);
            return attachmentDomainObject;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    @Override
    public DomainObject saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject) {
        InputStream contentStream = null;
        try {
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            return saveAttachment(contentStream, attachmentDomainObject);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    logger.error("Error closing input stream", e);
                }
            }

        }
    }

    protected DomainObject saveAttachment(InputStream contentStream, DomainObject attachmentDomainObject) {
        AccessToken accessToken = createSystemAccessToken();

        // Удаление старого вложения для не новых доменных объектов
        if (!attachmentDomainObject.isNew()){
            attachmentContentDao.deleteContent(attachmentDomainObject);
        }

        String fileName = attachmentDomainObject.getString(NAME);
        String attachmentType = attachmentDomainObject.getTypeName();
        Id parentId = attachmentDomainObject.getReference(configurationExplorer.getAttachmentParentType(attachmentType));
        DomainObject parentObject = domainObjectDao.find(parentId, accessToken);
        AttachmentInfo attachmentInfo = attachmentContentDao.saveContent(contentStream,
                parentObject, attachmentType, fileName);
        String newFilePath = attachmentInfo.getRelativePath();

        attachmentDomainObject.setString(MIME_TYPE, attachmentInfo.getMimeType());
        attachmentDomainObject.setLong(CONTENT_LENGTH, attachmentInfo.getContentLength());
        
        if (newFilePath == null || newFilePath.isEmpty()) {
            throw new FatalException("File isn't created. DO:" + attachmentDomainObject.getId());
        }
        attachmentDomainObject.setValue(PATH, new StringValue(newFilePath));

        return domainObjectDao.save(attachmentDomainObject, accessToken);
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("AttachmentService");
    }

    @Override
    public RemoteInputStream loadAttachment(Id attachmentDomainObjectId) {
        DomainObject attachmentDomainObject = crudService.find(attachmentDomainObjectId);
        try {
            checkAccessWithException(attachmentDomainObjectId, DomainObjectPermission.Permission.ReadAttachment);
            InputStream inFile = attachmentContentDao.loadContent(attachmentDomainObject);
            RemoteInputStream remoteInputStream = wrapStream(inFile);
            return remoteInputStream;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    protected abstract RemoteInputStream wrapStream(InputStream inputStream) throws java.rmi.RemoteException;

    @Override
    public void deleteAttachment(Id attachmentDomainObjectId) {
        try {
            checkAccessWithException(attachmentDomainObjectId, DomainObjectPermission.Permission.ReadAttachment);
            AccessToken accessToken = createSystemAccessToken();
            DomainObject attachmentObject = domainObjectDao.find(attachmentDomainObjectId, accessToken);
            domainObjectDao.delete(attachmentDomainObjectId, accessToken);
            attachmentContentDao.deleteContent(attachmentObject);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * Поиск вложений доменного объекта. Выполняет поиск всех вложеннний, указанных в цепочке наследования доменного
     * объекта.
     */
    @Override
    public List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId) {
        try {
            String domainObjectTypeName = domainObjectTypeIdCache.getName(domainObjectId);
            List<DomainObject> foundAttachments = new ArrayList<>();

            collectAttachmentsForDOAndParentDO(domainObjectId, domainObjectTypeName, foundAttachments);

            return sortById(foundAttachments);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * CMFIVE-35254
     * В некоторых случаях список вложений возвращается не в том порядке в каком они добавлены в документ.
     * Предположительно, связано с поведением Postgres. В связи с этим сортируем по Id принудительно.
     * @param objectsToSort
     * @return
     */
    private List<DomainObject> sortById(List<DomainObject> objectsToSort){
        if(objectsToSort!=null){
            Collections.sort(objectsToSort, (o1, o2) -> {
                RdbmsId obj1 = new RdbmsId(o1.getId());
                RdbmsId obj2 = new RdbmsId(o2.getId());
                if(obj1.getId() == (obj2.getId())){
                    return 0;
                }
                return (obj1.getId()<obj2.getId())?-1:1;
            });
        }
        return objectsToSort;
    }

    private void collectAttachmentsForDOAndParentDO(Id domainObjectId, String domainObjectTypeName,
                                                    List<DomainObject> attachmentDomainObjects) {

        findAttachmentsDeclaredInParticularDO(domainObjectId, domainObjectTypeName, attachmentDomainObjects);

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectTypeName);

        String parentDomainObjectType = domainObjectTypeConfig.getExtendsAttribute();
        if (parentDomainObjectType != null) {
            collectAttachmentsForDOAndParentDO(domainObjectId, parentDomainObjectType, attachmentDomainObjects);
        }
    }

    private void findAttachmentsDeclaredInParticularDO(Id domainObjectId, String domainObjectTypeName,
                                                       List<DomainObject> attachmentDomainObjects) {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectTypeName);

        if (domainObjectTypeConfig.getAttachmentTypesConfig() != null) {
            for (AttachmentTypeConfig attachmentTypeConfig : domainObjectTypeConfig.getAttachmentTypesConfig()
                    .getAttachmentTypeConfigs()) {
                DomainObjectTypeConfig attachDomainObjectTypeConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class, attachmentTypeConfig.getName());
                String attachmentType = attachDomainObjectTypeConfig.getName();
                List<DomainObject> domainObjectList = findAttachmentDomainObjectsFor(domainObjectId, attachmentType);
                if (domainObjectList != null) {
                    attachmentDomainObjects.addAll(domainObjectList);
                }
            }
        }
    }

    @Override
    public List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId, String attachmentType) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createAccessToken(user, domainObjectId, DomainObjectAccessType.READ);
            String domainObjectType = domainObjectTypeIdCache.getName(domainObjectId);

            String attachmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

            // Так как нам здесь важна сортировка результата по ID (CMFIVE-21464) вызываем метод с limit итерационно, затем складываем в один контейнер
            List<DomainObject> result = new ArrayList<>();
            // Размер пачки данных
            int batchSize = 100;
            // пачка с данными
            List<DomainObject> batch = null;
            // Номер пачкиы
            int batchNum = 0;
            do {
                batch = domainObjectDao.findLinkedDomainObjects(
                        domainObjectId, attachmentType, attachmentLinkedField, batchNum * batchSize, batchSize, accessToken);
                result.addAll(batch);
                batchNum++;
                // Если количество записей в пачке совпадает с переденным limit значит есть еще данные в хранилище, получаем итерационно
            }while(batch.size() == batchSize);

            return sortById(result);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObject findAttachmentDomainObjectFor(@Nonnull Id domainObjectId, @Nonnull String attachmentType, @Nonnull String attachmentName) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createAccessToken(user, domainObjectId, DomainObjectAccessType.READ);
            String domainObjectType = domainObjectTypeIdCache.getName(domainObjectId);

            String attachmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

            // Размер пачки данных
            int batchSize = 100;
            // пачка с данными
            List<DomainObject> batch = null;
            // Номер пачкиы
            int batchNum = 0;
            /* здесь будет результат поиска */
            DomainObject res;
            do {
                batch = domainObjectDao.findLinkedDomainObjects(
                        domainObjectId, attachmentType, attachmentLinkedField, batchNum * batchSize, batchSize, accessToken);
                res = batch.stream()
                        .filter(att -> att.getString(FIELD_ATTACH_NAME).equals(attachmentName))
                        .findAny().orElse(null);
                batchNum++;
                // Если количество записей в пачке совпадает с переденным limit значит есть еще данные в хранилище, получаем итерационно
            } while (batch.size() == batchSize && res == null);

            return res;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObject copyAttachment(Id attachmentDomainObjectId, Id destinationDomainObjectId, String destinationAttachmentType) {

        checkAccessWithException(attachmentDomainObjectId, DomainObjectPermission.Permission.ReadAttachment);

        DomainObject attachDomainObject = crudService.find(attachmentDomainObjectId);

        DomainObject attachmentCopyDomainObject = createAttachmentDomainObjectFor(destinationDomainObjectId, destinationAttachmentType);
        attachmentCopyDomainObject.setString(NAME, attachDomainObject.getString(NAME));
        attachmentCopyDomainObject.setString(MIME_TYPE, attachDomainObject.getString(MIME_TYPE));
        attachmentCopyDomainObject.setLong(CONTENT_LENGTH, attachDomainObject.getLong(CONTENT_LENGTH));
        attachmentCopyDomainObject.setString(DESCRIPTION, attachDomainObject.getString(DESCRIPTION));

        //RemoteInputStream remoteInputStream = loadAttachment(attachmentDomainObjectId);
        InputStream inputStream = attachmentContentDao.loadContent(attachDomainObject);
        try {
            attachmentCopyDomainObject = saveAttachment(inputStream, attachmentCopyDomainObject);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Error closing source stream", e);
            }
        }

        return attachmentCopyDomainObject;
    }

    @Override
    public List<DomainObject> copyAttachments(List<Id> attachmentDomainObjectIds, Id destinationDomainObjectId,
                                              String destinationAttachmentType) {
        if (attachmentDomainObjectIds == null || attachmentDomainObjectIds.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<DomainObject> attachmentCopyDomainObjects = new ArrayList<>(attachmentDomainObjectIds.size());

        for(Id attachmentDomainObjectId : attachmentDomainObjectIds) {

            if (!checkAccess(attachmentDomainObjectId, DomainObjectPermission.Permission.ReadAttachment)) {
                /* При массовом копировании вложений те, к которым нет доступа, просто пропускаем */
                continue;
            }

            DomainObject attachDomainObject =
                    copyAttachment(attachmentDomainObjectId, destinationDomainObjectId, destinationAttachmentType);
            attachmentCopyDomainObjects.add(attachDomainObject);
        }

        return attachmentCopyDomainObjects;
    }

    @Override
    public List<DomainObject> copyAttachmentsFrom(Id sourceDomainObjectId, String sourceAttachmentType,
                                           Id destinationDomainObjectId, String destinationAttachmentType) {
        List<DomainObject> attachmentDomainObjects =
                findAttachmentDomainObjectsFor(sourceDomainObjectId, sourceAttachmentType);
        return copyAttachments(extractIds(attachmentDomainObjects), destinationDomainObjectId, destinationAttachmentType);
    }

    @Override
    public List<DomainObject> copyAllAttachmentsFrom(Id sourceDomainObjectId, Id destinationDomainObjectId,
                                              String destinationAttachmentType){
        List<DomainObject> attachmentDomainObjects = findAttachmentDomainObjectsFor(sourceDomainObjectId);
        return copyAttachments(extractIds(attachmentDomainObjects), destinationDomainObjectId, destinationAttachmentType);
    }

    @Override
    public List<DomainObject> copyAllAttachmentsFrom(Id sourceDomainObjectId, Id destinationDomainObjectId,
                                              Map<String, String> attachmentTypeMap) {
        if (attachmentTypeMap == null || attachmentTypeMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<DomainObject> attachmentDomainObjects = new ArrayList<>();

        for (Map.Entry<String, String> entry : attachmentTypeMap.entrySet()) {
            attachmentDomainObjects.addAll(
                    copyAttachmentsFrom(sourceDomainObjectId, entry.getKey(), destinationDomainObjectId, entry.getValue()));
        }

        return attachmentDomainObjects;
    }

    private List<Id> extractIds(List<DomainObject> attachmentDomainObjects) {
        if (attachmentDomainObjects == null || attachmentDomainObjects.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<Id> attachmentCopyDomainObjectIds = new ArrayList<>(attachmentDomainObjects.size());

        for(DomainObject attachmentDomainObject : attachmentDomainObjects) {
            attachmentCopyDomainObjectIds.add(attachmentDomainObject.getId());
        }

        return attachmentCopyDomainObjectIds;
    }

    private String getAttachmentOwnerObject(String attachmentType, String domainObjectType) {
        DomainObjectTypeConfig objectConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);

        String declaringAttachmentDomainObject = null;
        if (objectConfig.getAttachmentTypesConfig() != null
                && objectConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().size() > 0) {

            for (AttachmentTypeConfig attachmentTypeConfig : objectConfig.getAttachmentTypesConfig()
                    .getAttachmentTypeConfigs()) {
                if (attachmentType.equals(attachmentTypeConfig.getName())) {
                    declaringAttachmentDomainObject = domainObjectType;
                    break;
                }
            }
        }

        if (declaringAttachmentDomainObject == null) {
            String parentType = objectConfig.getExtendsAttribute();
            if (parentType != null) {
                return getAttachmentOwnerObject(attachmentType, parentType);
            }
        }
        if (declaringAttachmentDomainObject == null) {
            throw new FatalException("Attachment declaration not found for " + attachmentType);
        }
        return declaringAttachmentDomainObject;
    }

    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public boolean checkAccess(Id attachId, Id userId, DomainObjectPermission.Permission permission) {

        if (userId == null || userGroupCache.isPersonSuperUser(userId)) {
            return true;
        }

        DomainObjectPermission permissions = permissionService.getObjectPermission(attachId, userId);
        return permissions.getPermission().contains(permission);
    }

    @Override
    public boolean checkAccess(Id attachId, DomainObjectPermission.Permission permission) {
        return checkAccess(attachId, currentUserAccessor.getCurrentUserId(), permission);
    }

    private void checkAccessWithException(Id attachId, DomainObjectPermission.Permission permission) {
        checkAccessWithException(attachId, currentUserAccessor.getCurrentUserId(), permission);
    }

    private void checkAccessWithException(Id attachId, Id userId, DomainObjectPermission.Permission permission) {
        if (!checkAccess(attachId, userId, permission)) {
            throw new AccessException(
                    "User " + userId.toStringRepresentation() + " have no permission " + permission.name()
                            + " to attachment " + attachId.toStringRepresentation());
        }
    }
}
