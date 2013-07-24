package ru.intertrust.cm.core.dao.impl;

import com.google.common.io.ByteStreams;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.exception.DaoException;

import java.io.*;
import java.util.Calendar;

/**
 * User: vlad
 */
public class FileSystemAttachmentContentDaoImpl implements AttachmentContentDao {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(FileSystemAttachmentContentDaoImpl.class);
    final private static boolean IS_WIN_OS = System.getProperty("os.name").toLowerCase().indexOf("win") > 0;

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
        String absFilePath = getAbsoluteFilePath(absDirPath, java.util.UUID.randomUUID().toString());
        FileOutputStream fos = null;
        try {
            File contentFile = new File(absFilePath);
            if (!contentFile.exists()) {
                contentFile.createNewFile();
            }
            fos = new FileOutputStream(contentFile);
            ByteStreams.copy(inputStream, fos);
        } catch (FileNotFoundException ex) {
            throw new DaoException(ex);
        } catch (IOException ex) {
            throw new DaoException(ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
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
        String fileName = ((StringValue) domainObject.getValue("path")).get();
        File f = new File(fileName);
        if (f.exists()) {
            try {
                f.delete();
            } catch (RuntimeException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private String getAbsoluteDirPath() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        StringBuilder sb = new StringBuilder();
        sb.append(attachmentSaveLocation);
        if (!attachmentSaveLocation.endsWith(getBackslash()))
            sb.append(getBackslash());
        sb.append(year);
        sb.append(getBackslash());
        sb.append(month);
        sb.append(getBackslash());
        sb.append(day);
        return sb.toString();
    }

    private String getAbsoluteFilePath(String absDirPath, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(absDirPath);
        if (!absDirPath.endsWith(getBackslash()))
            sb.append(getBackslash());
        sb.append(fileName);
        return sb.toString();
    }

    static private String getBackslash() {
       return IS_WIN_OS ? "\\" : "/";
    }
}
