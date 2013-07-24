package ru.intertrust.cm.core.business.impl;

import com.google.common.base.Strings;
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

    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CrudService crudService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public DomainObject createAttachmentDomainObjectFor(String domainObjectType) {
        return crudService.createDomainObject(domainObjectType);
    }

    @Override
    public void saveAttachment(RemoteInputStream inputStream, DomainObject attachmentDomainObject) {
        InputStream contentStream = null;
        String newFilePath = null;
        try {
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            newFilePath = attachmentContentDao.saveContent(contentStream);
            //если newFilePath is null или empty не обрабатываем
            if (Strings.isNullOrEmpty(newFilePath)) {
                throw new DaoException("File isn't created");
            }
            //предыдущий файл удаляем
            StringValue oldFilePath = (StringValue) attachmentDomainObject.getValue("path");
            if (oldFilePath != null && !oldFilePath.isEmpty()) {
                //файл может быть и не удален
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            attachmentDomainObject.setValue("path", new StringValue(newFilePath));
            domainObjectDao.save(attachmentDomainObject);
        } catch (IOException ex) {
            if (!Strings.isNullOrEmpty(newFilePath)) {
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
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObject.getTypeName());
        if (domainObjectTypeConfig.getAttachmentTypesConfig() == null) {
            return Collections.emptyList();
        }
        List<DomainObject> attachmentDomainObjects = new ArrayList<>();
        for (AttachmentTypeConfig attachmentTypeConfig : domainObjectTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
            DomainObjectTypeConfig attachDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, attachmentTypeConfig.getName());
            attachmentDomainObjects.addAll(domainObjectDao.findChildren(domainObject.getId(), attachDomainObjectTypeConfig.getName()));
        }
        return attachmentDomainObjects;
    }
}