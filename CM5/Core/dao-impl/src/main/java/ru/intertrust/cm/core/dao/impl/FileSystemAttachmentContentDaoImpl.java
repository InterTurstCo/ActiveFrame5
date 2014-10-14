package ru.intertrust.cm.core.dao.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.exception.DaoException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: vlad
 * свойство path в DomainObject - относительный путь к файлам.
 * свойство attachmentSaveLocation - должно отображать корневой путь к папкам,
 * где сохраняются вложенные файлы.
 */
public class FileSystemAttachmentContentDaoImpl implements AttachmentContentDao {

    final private static org.slf4j.Logger logger = LoggerFactory.getLogger(FileSystemAttachmentContentDaoImpl.class);
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    final static private String PATH_NAME = "Path";
    
    @org.springframework.beans.factory.annotation.Value("${attachment.storage}")
    private String attachmentSaveLocation;
    
    @Autowired
    ConfigurationExplorer configurationExplorer;
        
    @Autowired
    private UserTransactionService userTransactionService;
    
    @Autowired
    private EventLogService eventLogService;
    
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setAttachmentSaveLocation(String attachmentSaveLocation) {
        this.attachmentSaveLocation = attachmentSaveLocation;
    }
        
    public void setEventLogService(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    private void init() {
        //Заменяем настройку путей на использование server.properties. значение устанавливает аннотация @Value("#{attachment.storage}")
        /*GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();
        AttachmentStorageConfig storageConfig = globalSettings.getAttachmentStorageConfig();
        attachmentSaveLocation = storageConfig.getPath();*/
    }
    
    @Override
    public String saveContent(InputStream inputStream) {
        String absDirPath = getAbsoluteDirPath();
        File dir = new File(absDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String absFilePath = newAbsoluteFilePath(absDirPath);
        userTransactionService.addListenerForSaveFile(absFilePath);
        try {
            //не заменяет файл
            Files.copy(inputStream, Paths.get(absFilePath));
        } catch (IOException ex) {
            throw new DaoException(ex);
        }

        return toRelativeFromAbsPathFile(absFilePath);
    }

    @Override
    public InputStream loadContent(DomainObject domainObject) {
        try {
            if (!configurationExplorer.isAttachmentType(domainObject.getTypeName())) {
                throw new DaoException("DomainObject is not attachment: " + domainObject);

            }
            if (isPathEmptyInDo(domainObject)) {
                throw new DaoException("The path is empty");
            }
            String relFilePath = ((StringValue) domainObject.getValue(PATH_NAME)).get();
            FileInputStream inputStream = new FileInputStream(toAbsFromRelativePathFile(relFilePath));
            eventLogService.logDownloadAttachmentEvent(domainObject.getId());
            return inputStream;
            
        } catch (FileNotFoundException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        if (isPathEmptyInDo(domainObject)) {
            return;
        }
        Value value = domainObject.getValue(PATH_NAME);
        String relFilePath = ((StringValue) value).get();
        File f = new File(toAbsFromRelativePathFile(relFilePath));
        if (f.exists()) {
            try {
                f.delete();
            } catch (RuntimeException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private boolean isPathEmptyInDo(DomainObject domainObject) {
        Value value = domainObject.getValue(PATH_NAME);
        return value == null || value.isEmpty() || !(value instanceof StringValue);
    }

    private String getAbsoluteDirPath() {
        return Paths.get(attachmentSaveLocation, formatter.format(new Date())).toAbsolutePath().toString();
    }

    private String newAbsoluteFilePath(String absDirPath) {
        Path fs;
        do {
            fs = Paths.get(absDirPath, java.util.UUID.randomUUID().toString());
        } while (Files.exists(fs, LinkOption.NOFOLLOW_LINKS));
        return fs.toAbsolutePath().toString();
    }

    private String toRelativeFromAbsPathFile(String absFilePath) {

        if (Paths.get(absFilePath).startsWith(Paths.get(attachmentSaveLocation))) {
            return absFilePath.substring(attachmentSaveLocation.length());
        } else {
            return absFilePath;
        }
    }

    private String toAbsFromRelativePathFile(String relFilePath) {
        return Paths.get(attachmentSaveLocation, relFilePath).toAbsolutePath().toString();
    }
}
