package ru.intertrust.cm.core.dao.impl;

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

    final private static org.slf4j.Logger logger = LoggerFactory.getLogger(FileSystemAttachmentContentDaoImpl.class);
    private static final int BUF_SIZE = 0x1000;
    private static String fileSeparator = File.separator;

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
            copyStreamToFile(inputStream, fos);
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
        if (!attachmentSaveLocation.endsWith(fileSeparator))
            sb.append(fileSeparator);
        sb.append(year);
        sb.append(fileSeparator);
        sb.append(month);
        sb.append(fileSeparator);
        sb.append(day);
        return sb.toString();
    }

    private String getAbsoluteFilePath(String absDirPath, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(absDirPath);
        if (!absDirPath.endsWith(fileSeparator))
            sb.append(fileSeparator);
        sb.append(fileName);
        return sb.toString();
    }

    private static long copyStreamToFile(InputStream from, OutputStream to)
            throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
}
