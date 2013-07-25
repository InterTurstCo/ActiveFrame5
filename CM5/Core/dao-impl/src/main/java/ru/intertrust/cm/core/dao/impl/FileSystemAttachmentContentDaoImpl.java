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
        String absFilePath = getAbsoluteFilePath(absDirPath);
        try {
            //не заменяет файл
            Files.copy(inputStream, Paths.get(absFilePath));
        } catch (FileNotFoundException ex) {
            throw new DaoException(ex);
        } catch (IOException ex) {
            throw new DaoException(ex);
        }
        return absFilePath;
    }

    @Override
    public InputStream loadContent(DomainObject domainObject) {
        FileInputStream fstream = null;
        try {
            String fileName = ((StringValue) domainObject.getValue("path")).get();
            fstream = new FileInputStream(fileName);
            return fstream;
        } catch (FileNotFoundException ex) {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            throw new DaoException(ex);
        }
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        Value value = domainObject.getValue("path");
        if (value == null || value.isEmpty() || !(value instanceof StringValue)) {
            return;
        }
        String pathName = ((StringValue) value).get();
        File f = new File(pathName);
        if (f.exists()) {
            try {
                f.delete();
            } catch (RuntimeException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private String getAbsoluteDirPath() {
        return Paths.get(attachmentSaveLocation, formatter.format(new Date())).toAbsolutePath().toString();
    }

    private String getAbsoluteFilePath(String absDirPath) {
        Path fs;
        do {
            fs = Paths.get(absDirPath, java.util.UUID.randomUUID().toString());
        } while (Files.exists(fs, LinkOption.NOFOLLOW_LINKS));
        return fs.toAbsolutePath().toString();
    }
}
