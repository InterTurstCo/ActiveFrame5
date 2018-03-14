package ru.intertrust.cm.core.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;

/**
 * А.П. Этот класс более не используется. Реализация интерфейса {@link AttachmentContentDao} сделана обобщённой,
 * поддерживающей несколько хранилищ вложений, и перенесена в класс {@link ru.intertrust.cm.core.dao.impl.attach.AttachmentContentDaoImpl}.
 * Хранилище вложений в папках файловой системы реализовано в классе {@link ru.intertrust.cm.core.dao.impl.attach.FileSystemAttachmentStorageImpl}.
 * 
 * User: vlad
 * свойство path в DomainObject - относительный путь к файлам.
 * свойство attachmentSaveLocation - должно отображать корневой путь к папкам,
 * где сохраняются вложенные файлы.
 */
public class FileSystemAttachmentContentDaoImpl /*implements AttachmentContentDao*/ {

    private static final String DATE_PATTERN = "yyyy/MM/dd";
    final private static org.slf4j.Logger logger = LoggerFactory.getLogger(FileSystemAttachmentContentDaoImpl.class);
    final static private String PATH_NAME = "Path";

    @org.springframework.beans.factory.annotation.Value("${attachment.storage}")
    private String attachmentSaveLocation;

    @org.springframework.beans.factory.annotation.Value("${attachments.strict.mode:true}")
    private boolean attachmentsStrictMode;
    
    
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

    //@Override
    public AttachmentInfo saveContent(InputStream inputStream, String fileName) {
        AttachmentInfo attachmentInfo = new AttachmentInfo();
        String absDirPath = getAbsoluteDirPath();
        File dir = new File(absDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final String absFilePath = newAbsoluteFilePath(absDirPath, getFileExtension(fileName));
        userTransactionService.addListener(new BaseActionListener() {
            @Override
            public void onRollback() {
                File f = new File(absFilePath);
                if (f.exists()) {
                    try {
                        f.delete();
                    } catch (RuntimeException ex) {
                        logger.error("Error deleting uncommitted content on transaction rollback", ex);
                    }
                }
            }
        });
        try {
            //не заменяет файл
            Files.copy(inputStream, Paths.get(absFilePath));
            File attachment = new File(absFilePath);
            Long contentLength = attachment.length();
            String mimeType = getMimeType(absFilePath);
            String relativePath = toRelativeFromAbsPathFile(absFilePath);
            attachmentInfo.setContentLength(contentLength);
            attachmentInfo.setMimeType(mimeType);
            attachmentInfo.setRelativePath(relativePath);
            
        } catch (IOException ex) {
            throw new DaoException(ex);
        }

        return attachmentInfo;
    }

    private String getMimeType(String path){
        Tika detector = new Tika();
        return detector.detect(path);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String fileExtension = null;
        Integer dropIndex = fileName.lastIndexOf(".");
        if (dropIndex > 0) {
            fileExtension = fileName.substring(dropIndex);

        }
        return fileExtension;
    }

    //@Override
    public InputStream loadContent(DomainObject domainObject) {
        try {
            if (!configurationExplorer.isAttachmentType(domainObject.getTypeName())) {
                throw new DaoException("DomainObject is not attachment: " + domainObject);

            }
            if (isPathEmptyInDo(domainObject)) {
                throw new DaoException("The path is empty");
            }
            String relFilePath = ((StringValue) domainObject.getValue(PATH_NAME)).get();
            File contentFile = new File(toAbsFromRelativePathFile(relFilePath));
            InputStream inputStream = null;
            if (contentFile.exists()){
                inputStream = new FileInputStream(contentFile);
            }else{
                if (attachmentsStrictMode){
                    throw new FatalException("Error load content. File " + contentFile.getPath() + " not exists.");
                }else{
                    inputStream = new ByteArrayInputStream(new byte[0]);
                }
            }
            eventLogService.logDownloadAttachmentEvent(domainObject.getId());
            return inputStream;
            
        } catch (FileNotFoundException e) {
            throw new DaoException(e);
        }
    }

    //@Override
    public void deleteContent(DomainObject domainObject) {
        if (isPathEmptyInDo(domainObject)) {
            return;
        }
        StringValue value = domainObject.getValue(PATH_NAME);
        String relFilePath = value.get();
        
        logger.debug("Delete content " + relFilePath);
        if (logger.isTraceEnabled()){
            String message = "";
            for(StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {                         
                message += System.lineSeparator() + "\t" + stackTraceElement.toString();
            }            
            logger.trace(message);
        }        
        
        final File f = new File(toAbsFromRelativePathFile(relFilePath));
        if (f.exists()) {
            try {
                userTransactionService.addListener(new BaseActionListener() {
                    @Override
                    public void onAfterCommit() {
                        try {
                            f.delete();
                        } catch (RuntimeException e) {
                            logger.error("Error deleting attachment content", e);
                        }
                    }
                });
            } catch (RuntimeException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    //@Override
    public String toRelativeFromAbsPathFile(String absFilePath) {
        String relativePath = Paths.get(absFilePath).startsWith(Paths.get(attachmentSaveLocation)) ?
                absFilePath.substring(attachmentSaveLocation.length()) : absFilePath;

        if (relativePath.startsWith("\\")) {
            relativePath = FilenameUtils.separatorsToUnix(relativePath);
        }

        return relativePath;
    }

    private boolean isPathEmptyInDo(DomainObject domainObject) {
        Value<?> value = domainObject.getValue(PATH_NAME);
        return value == null || value.isEmpty() || !(value instanceof StringValue);
    }

    private String getAbsoluteDirPath() {
        return Paths.get(attachmentSaveLocation, ThreadSafeDateFormat.format(new Date(), DATE_PATTERN)).toAbsolutePath().toString();
    }

    private String newAbsoluteFilePath(String absDirPath, String extension) {
        Path fs;
        do {
            String nameCandidate = UUID.randomUUID().toString();
            if (extension != null) {
                nameCandidate += extension;
            }
            fs = Paths.get(absDirPath, nameCandidate);
        } while (Files.exists(fs, LinkOption.NOFOLLOW_LINKS));
        return fs.toAbsolutePath().toString();
    }

    private String toAbsFromRelativePathFile(String relFilePath) {
        return Paths.get(attachmentSaveLocation, relFilePath).toAbsolutePath().toString();
    }
}
