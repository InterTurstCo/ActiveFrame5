package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.UnexpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 25.04.14.
 */
public abstract class BaseAttachmentServiceImpl implements BaseAttachmentService {
    final static public String PATH_NAME = "Path";
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(RemoteAttachmentServiceImpl.class);
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

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public DomainObject createAttachmentDomainObjectFor(Id objectId, String attachmentType) {
        try {
            GenericDomainObject attachmentDomainObject = (GenericDomainObject) crudService.createDomainObject(attachmentType);

            String domainObjectType = domainObjectTypeIdCache.getName(objectId);

            String attchmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

            attachmentDomainObject.setReference(attchmentLinkedField, objectId);
            return attachmentDomainObject;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in createAttachmentDomainObjectFor", ex);
            throw new UnexpectedException("AttachmentService", "createAttachmentDomainObjectFor",
                    "objectId:" + objectId + " attachmentType:" + attachmentType, ex);
        }
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public DomainObject saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject) {
        InputStream contentStream = null;
        StringValue newFilePathValue = null;
        DomainObject savedDoaminObject = null;
        try {
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            String newFilePath = attachmentContentDao.saveContent(contentStream);
            //если newFilePath is null или empty не обрабатываем
            if (newFilePath == null || newFilePath.isEmpty()) {
                throw new UnexpectedException("File isn't created. DO:" + attachmentDomainObject.getId());
            }
            newFilePathValue = new StringValue(newFilePath);
            StringValue oldFilePathValue = (StringValue) attachmentDomainObject.getValue("path");
            attachmentDomainObject.setValue(PATH_NAME, new StringValue(newFilePath));
            AccessToken accessToken = createSystemAccessToken();

            savedDoaminObject = domainObjectDao.save(attachmentDomainObject, accessToken);

            //предыдущий файл удаляем
            if (oldFilePathValue != null && !oldFilePathValue.isEmpty()) {
                //файл может быть и не удален, в случае если заблокирован
                attachmentDomainObject.setValue(PATH_NAME, oldFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            savedDoaminObject.setValue("path", newFilePathValue);
            return savedDoaminObject;
        } catch (IOException ex) {
            if (newFilePathValue != null && !newFilePathValue.isEmpty()) {
                attachmentDomainObject.setValue(PATH_NAME, newFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            logger.error("Unexpected exception caught in saveAttachment", ex);
            throw new UnexpectedException("AttachmentService", "saveAttachment",
                    "attachmentDomainObject:" + attachmentDomainObject.getId(), ex);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in saveAttachment", ex);
            throw new UnexpectedException("AttachmentService", "saveAttachment",
                    "attachmentDomainObject:" + attachmentDomainObject.getId(), ex);
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }

        }
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("AttachmentService");
    }

    public RemoteInputStream loadAttachment(Id attachmentDomainObjectId) {
        InputStream inFile = null;
        SimpleRemoteInputStream remoteInputStream = null;
        DomainObject attachmentDomainObject = crudService.find(attachmentDomainObjectId);
        try {
            InputStream inputStream = attachmentContentDao.loadContent(attachmentDomainObject);
            RemoteInputStream export = wrapStream(inputStream);
            return export;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in loadAttachment", ex);
            if (inFile != null) {
                try {
                    inFile.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (remoteInputStream != null) {
                remoteInputStream.close();
            }
            throw new UnexpectedException("AttachmentService", "loadAttachment",
                    "attachmentDomainObjectId:" + attachmentDomainObjectId, ex);
        }
    }

    abstract RemoteInputStream wrapStream(InputStream inputStream) throws java.rmi.RemoteException;

    public void deleteAttachment(Id attachmentDomainObjectId) {
        try {
            AccessToken accessToken = createSystemAccessToken();
            // [CMFIVE-705, 02/04/14] crudService.find(..) replaced with domainObjectDao.find(..) as crudService refers
            // to EjbContext which is unavailable at reports deployment time
            //DomainObject attachmentDomainObject = crudService.find(attachmentDomainObjectId);
            DomainObject attachmentDomainObject = domainObjectDao.find(attachmentDomainObjectId, accessToken);
            attachmentContentDao.deleteContent(attachmentDomainObject);
            //файл может быть и не удален
            domainObjectDao.delete(attachmentDomainObjectId, accessToken);
        } catch (DaoException ex) {
            logger.error("Unexpected exception caught in deleteAttachment", ex);
            throw new UnexpectedException("AttachmentService", "deleteAttachment",
                    "attachmentDomainObjectId:" + attachmentDomainObjectId, ex);
        }
    }

    /**
     * Поиск вложений доменного объекта. Выполняет поиск всех вложеннний, указанных в цепочке наследования доменного
     * объекта.
     */
    public List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId) {
        try {
            String domainObjectTypeName = domainObjectTypeIdCache.getName(domainObjectId);
            List<DomainObject> foundAttachments = new ArrayList<>();

            collectAttachmentsForDOAndParentDO(domainObjectId, domainObjectTypeName, foundAttachments);
            return foundAttachments;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findAttachmentDomainObjectsFor", ex);
            throw new UnexpectedException("AttachmentService", "findAttachmentDomainObjectsFor",
                    "domainObjectId:" + domainObjectId, ex);
        }
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

    public List<DomainObject> findAttachmentDomainObjectsFor(Id domainObjectId, String attachmentType) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createAccessToken(user, domainObjectId, DomainObjectAccessType.READ);
            String domainObjectType = domainObjectTypeIdCache.getName(domainObjectId);

            String attchmentLinkedField = getAttachmentOwnerObject(attachmentType, domainObjectType);

            return domainObjectDao.findLinkedDomainObjects(domainObjectId, attachmentType, attchmentLinkedField, accessToken);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findAttachmentDomainObjectsFor", ex);
            throw new UnexpectedException("AttachmentService", "findAttachmentDomainObjectsFor",
                    "domainObjectId:" + domainObjectId + " attachmentType:" + attachmentType, ex);
        }
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
}
