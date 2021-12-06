package ru.intertrust.cm.core.dao.impl.attach;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.AttachmentStorageTypeConfig;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;

public class AttachmentContentDaoImpl implements AttachmentContentDao {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentContentDaoImpl.class);

    @Autowired
    private ConfigurationExplorer confExplorer;
    @Autowired
    private AttachmentStorageConfigHelper confHelper;
    @Autowired
    private DomainObjectTypeIdCache typeCache;
    @Autowired
    private EventLogService eventLogService;

    @Autowired
    private ApplicationContext appContext;

    @Value("${attachments.alternate.storage:none}")
    private String findStorage;
    @Value("${attachments.strict.mode:true}")
    private boolean strictMode;

    private static final String FIND_NONE = "none";
    private static final String FIND_EVERYWHERE = "all";

    private Map<String, AttachmentStorage> storages = new HashMap<>();

    private String[] alternateStorages;

    @PostConstruct
    public void initialize() {
        for (AttachmentStorageConfig storageConfig : confExplorer.getConfigs(AttachmentStorageConfig.class)) {
            String name = storageConfig.getName();
            storages.put(name, createStorage(name, storageConfig.getStorageConfig()));
        }
        String findStorage = this.findStorage.trim();
        if (FIND_NONE.equalsIgnoreCase(findStorage)) {
            alternateStorages = new String[0];
        } else if (FIND_EVERYWHERE.equalsIgnoreCase(findStorage)) {
            alternateStorages = storages.keySet().toArray(new String[storages.size()]);
        } else {
            alternateStorages = findStorage.split("\\s*[,;]\\s*");
            for (String name : alternateStorages) {
                if (!storages.containsKey(name)) {
                    throw new ConfigurationException("Storage " + name + " referred by attachments.find.storage property doesn't exist");
                }
            }
        }
    }

    private AttachmentStorage createStorage(String name, AttachmentStorageTypeConfig config) {
        AttachmentStorage storage;
        if (config instanceof FolderStorageConfig) {
            storage = new FileSystemAttachmentStorageImpl(name, (FolderStorageConfig) config);
        } else {
            throw new ConfigurationException("Unsupported storage type configuration: " + config.getClass().getName());
        }
        AutowireCapableBeanFactory factory = appContext.getAutowireCapableBeanFactory();
        factory.autowireBean(storage);
        factory.initializeBean(storage, name + "AttachmentStorage");
        return storage;
    }

    @Override
    public AttachmentInfo saveContent(InputStream inputStream, DomainObject parentObject, String attachmentType, String fileName) {
        if (!confExplorer.isAttachmentType(attachmentType)) {
            throw new DaoException(attachmentType + " is not an attachment type");
        }
        String storageName = confHelper.getStorageForAttachment(attachmentType, parentObject.getTypeName());
        AttachmentStorage storage = storages.get(storageName);
        if (storage == null) {
            throw new DaoException("Storage for " + attachmentType + " is not configured");
        }
        return storage.saveContent(inputStream, new AttachmentStorage.StaticContext()
                .attachmentType(attachmentType).parentObject(parentObject).fileName(fileName).creationTime());
    }

    @Override
    public InputStream loadContent(DomainObject domainObject) {
        if (!confExplorer.isAttachmentType(domainObject.getTypeName())) {
            throw new DaoException(domainObject.getTypeName() + " is not an attachment type");
        }
        String attachmentType = domainObject.getTypeName();
        String storageName = confHelper.getStorageForAttachment(attachmentType, getParentObjectType(domainObject));
        String localPath = domainObject.getString("Path");
        InputStream result = null;
        try {
            result = storages.get(storageName).getContent(localPath);
        } catch (FileNotFoundException e) {
            AttachmentStorage altStorage = findInAlternateStorage(domainObject, storageName);
            if (altStorage != null) {
                try {
                    result = altStorage.getContent(localPath);
                } catch (FileNotFoundException ee) {
                    logger.warn("Error reading attachment " + localPath + " from alternate storage", ee);
                }
            }
            if (result == null) {
                if (strictMode) {
                    throw new FatalException("Error load content for path " + localPath, e);
                }
                logger.warn("Attachment " + localPath + " not found; empty content returned");
                result = new ByteArrayInputStream(new byte[0]);
            }
        }
        eventLogService.logDownloadAttachmentEvent(domainObject.getId());
        return result;
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        if (!confExplorer.isAttachmentType(domainObject.getTypeName())) {
            throw new DaoException(domainObject.getTypeName() + " is not an attachment type");
        }
        String attachmentType = domainObject.getTypeName();
        String storageName = confHelper.getStorageForAttachment(attachmentType, getParentObjectType(domainObject));
        String localPath = domainObject.getString("Path");
        boolean found = storages.get(storageName).deleteContent(localPath);
        if (!found) {
            AttachmentStorage altStorage = findInAlternateStorage(domainObject, storageName);
            if (altStorage != null) {
                altStorage.deleteContent(localPath);
            }
        }
    }

    private String getParentObjectType(DomainObject attachment) {
        String parentRefField = confExplorer.getAttachmentParentType(attachment.getTypeName());
        Id parentId = attachment.getReference(parentRefField);
        return typeCache.getName(parentId);
    }

    protected String[] getAlternateStorageNames() {
        return alternateStorages;
    }

    protected AttachmentStorage getStorageByName(String name) {
        return storages.get(name);
    }

    protected AttachmentStorage findInAlternateStorage(DomainObject attachment, String defaultStorageName) {
        if (alternateStorages.length == 0) {
            return null;
        }
        AttachmentInfo info = new AttachmentInfo();
        info.setRelativePath(attachment.getString("Path"));
        info.setContentLength(attachment.getLong("ContentLength"));
        return findInAlternateStorage(info, defaultStorageName);
    }

    protected AttachmentStorage findInAlternateStorage(AttachmentInfo info, String defaultStorageName) {
        logger.warn("Attachment " + info.getRelativePath() + " not found in storage " + defaultStorageName
                + "; trying alternate storages");
        for (String altName : alternateStorages) {
            if (!altName.equals(defaultStorageName)) {
                AttachmentStorage storage = storages.get(altName);
                if (storage.hasContent(info)) {
                    logger.info("Attachment " + info.getRelativePath() + " found in storage " + altName);
                    return storage;
                }
            }
        }
        return null;
    }
}
