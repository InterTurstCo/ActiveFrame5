package ru.intertrust.cm.core.dao.impl;

import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FileUtils;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.exception.DaoException;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * User: vlad
 * свойство path в DomainObject - относительный путь к файлам.
 * свойство attachmentSaveLocation - должно отображать корневой путь к папкам,
 * где сохраняются вложенные файлы.
 */
public class FileSystemAttachmentContentDaoImpl implements AttachmentContentDao {

    final private static org.slf4j.Logger logger = LoggerFactory.getLogger(FileSystemAttachmentContentDaoImpl.class);
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    private String attachmentSaveLocation;

    public void setAttachmentSaveLocation(String attachmentSaveLocation) {
        this.attachmentSaveLocation = attachmentSaveLocation;
    }

    @Override
    public String saveContent(InputStream inputStream) {
        String absDirPath = getAbsoluteDirPath();
        File dir = new File(absDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String absFilePath = newAbsoluteFilePath(absDirPath);
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
            if (isPathEmptyInDo(domainObject)) {
                throw new DaoException("The path is empty");
            }
            String relFilePath = ((StringValue) domainObject.getValue("path")).get();
            return new FileInputStream(toAbsFromRelativePathFile(relFilePath));
        } catch (FileNotFoundException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        if (isPathEmptyInDo(domainObject)) {
            return;
        }
        Value value = domainObject.getValue("path");
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
        Value value = domainObject.getValue("path");
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
        if (absFilePath.startsWith(attachmentSaveLocation)) {
            return absFilePath.substring(attachmentSaveLocation.length() + 1);
        } else {
            return absFilePath;
        }
    }

    private String toAbsFromRelativePathFile(String relFilePath) {
        return Paths.get(attachmentSaveLocation, relFilePath).toAbsolutePath().toString();
    }
}
