package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.exception.DaoException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: vlad
 */
@Stateless
@Local(AttachmentService.class)
@Remote(AttachmentService.Remote.class)
public class AttachmentServiceImpl implements AttachmentService {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    final static private String PATH_NAME = "Path";

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

    @Override
    public DomainObject createAttachmentDomainObjectFor(String domainObjectType) {
        return crudService.createDomainObject(domainObjectType);
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public void saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject) {
        InputStream contentStream = null;
        StringValue newFilePathValue = null;
        try {
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            String newFilePath = attachmentContentDao.saveContent(contentStream);
            //если newFilePath is null или empty не обрабатываем
            if (newFilePath == null || newFilePath.isEmpty()) {
                throw new DaoException("File isn't created");
            }
            newFilePathValue = new StringValue(newFilePath);
            StringValue oldFilePathValue = (StringValue) attachmentDomainObject.getValue("path");
            attachmentDomainObject.setValue(PATH_NAME, new StringValue(newFilePath));
            domainObjectDao.save(attachmentDomainObject);
            //предыдущий файл удаляем
            if (oldFilePathValue != null && !oldFilePathValue.isEmpty()) {
                //файл может быть и не удален, в случае если заблокирован
                attachmentDomainObject.setValue(PATH_NAME, oldFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            attachmentDomainObject.setValue("path", newFilePathValue);
        } catch (IOException ex) {
            if (newFilePathValue != null && !newFilePathValue.isEmpty()) {
                attachmentDomainObject.setValue(PATH_NAME, newFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            throw new DaoException(ex.getMessage());
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

    @Override
    public RemoteInputStream loadAttachment(DomainObject attachmentDomainObject) {
        InputStream inFile = null;
        SimpleRemoteInputStream remoteInputStream = null;
        try {
            inFile = attachmentContentDao.loadContent(attachmentDomainObject);
            remoteInputStream = new SimpleRemoteInputStream(inFile);
            return remoteInputStream.export();
        } catch (RemoteException ex) {
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
            throw new DaoException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttachment(DomainObject attachmentDomainObject) {
        attachmentContentDao.deleteContent(attachmentDomainObject);
        //файл может быть и не удален
        domainObjectDao.delete(attachmentDomainObject.getId());
    }

    @Override
    public List<DomainObject> getAttachmentDomainObjectsFor(DomainObject domainObject) {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObject.getTypeName());
        if (domainObjectTypeConfig.getAttachmentTypesConfig() == null) {
            return Collections.emptyList();
        }
        List<DomainObject> attachmentDomainObjects = new ArrayList<>();
        for (AttachmentTypeConfig attachmentTypeConfig :
                domainObjectTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
            DomainObjectTypeConfig attachDomainObjectTypeConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, attachmentTypeConfig.getName());
            //TODO get userId from EJB Context  
            int userId = 1;
            AccessToken accessToken = accessControlService.createAccessToken(userId, domainObject.getId(), DomainObjectAccessType.READ);
            attachmentDomainObjects.addAll(
                    domainObjectDao.findChildren(domainObject.getId(), attachDomainObjectTypeConfig.getName(), accessToken));
        }
        return attachmentDomainObjects;
    }
}