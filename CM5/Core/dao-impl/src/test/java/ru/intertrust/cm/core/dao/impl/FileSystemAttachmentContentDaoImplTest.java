package ru.intertrust.cm.core.dao.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Vlad
 */
@RunWith(MockitoJUnitRunner.class)
public class FileSystemAttachmentContentDaoImplTest {

    static private final String TEST_OUT_DIR = System.getProperty("test.cnf.testOutDir");

    @Test
    public void saveContent() throws IOException {
        FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
        String path = contentDao.saveContent(bis);
        Files.copy(Paths.get(path), bos);
        Assert.assertArrayEquals(expBytes, bos.toByteArray());
    }

    @Test
    public void loadContent() throws IOException {
        FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        String path = createAndCopyToFile(new ByteArrayInputStream(expBytes));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Files.copy(Paths.get(path), bos);
        DomainObject domainObject = new GenericDomainObject();
        domainObject.setValue("path", new StringValue(path));
        InputStream inputStream = contentDao.loadContent(domainObject);
        bos.reset();
        int count;
        byte[] bs = new byte[10];
        while ((count = inputStream.read(bs)) != -1) {
            bos.write(bs, 0, count);
        }
        Assert.assertArrayEquals(expBytes, bos.toByteArray());
    }

    @Test
    public void deleteContent() throws IOException {
        FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        String path = createAndCopyToFile(new ByteArrayInputStream(expBytes));
        Assert.assertTrue(new File(path).exists());
        DomainObject domainObject = new GenericDomainObject();
        domainObject.setValue("path", new StringValue(path));
        contentDao.deleteContent(domainObject);
        Assert.assertFalse(new File(path).exists());
    }

    private String createAndCopyToFile(InputStream inputStream) throws IOException {
        Path path = Paths.get(TEST_OUT_DIR, "FileSystemAttachmentContentDaoImplTest");
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        return path.toAbsolutePath().toString();
    }
}
