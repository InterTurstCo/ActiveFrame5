package ru.intertrust.cm.core.dao.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;

/**
 * @author Vlad
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"classpath:test_beans.xml"})
public class FileSystemAttachmentContentDaoImplTest {

    private static final String NOT_ATTACHMENT_TYPE = "NotAttachment";
    static private final String TEST_OUT_DIR = System.getProperty("test.cnf.testOutDir");
    final static private String PATH_NAME = "Path";

    @InjectMocks
    FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
    
    @Mock
    private ConfigurationExplorerImpl configurationExplorer;

    @Mock
    private EventLogService eventLogService;
    
    @Mock
    private UserTransactionService userTransactionService;

    
    @Test
    public void saveContent() throws IOException {
/*        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:test_beans.xml");
        ctx.refresh();
        FileSystemAttachmentContentDaoImpl contentDao = (FileSystemAttachmentContentDaoImpl) ctx.getBean("attachmentContentDao");
*/      contentDao.setAttachmentSaveLocation(TEST_OUT_DIR);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        ByteArrayInputStream bis = new ByteArrayInputStream(expBytes);
        AttachmentInfo attachmentInfo = contentDao.saveContent(bis, "test_file.jpg");
        String path = attachmentInfo.getRelativePath();
        Files.copy(Paths.get(TEST_OUT_DIR, path), bos);
        Assert.assertArrayEquals(expBytes, bos.toByteArray());
    }

    @Test
    public void loadContent() throws IOException {
        when(configurationExplorer.isAttachmentType(anyString())).thenReturn(true);
        contentDao.setAttachmentSaveLocation(TEST_OUT_DIR);
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        String path = createAndCopyToFile(new ByteArrayInputStream(expBytes));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Files.copy(Paths.get(path), bos);
        DomainObject domainObject = new GenericDomainObject();
        String relPath = Paths.get(path).subpath(Paths.get(TEST_OUT_DIR).getNameCount(), Paths.get(path).getNameCount()).toString();
        domainObject.setValue(PATH_NAME, new StringValue(relPath));
        InputStream inputStream = null;
        try {
            inputStream = contentDao.loadContent(domainObject);
            bos.reset();
            int count;
            byte[] bs = new byte[10];
            while ((count = inputStream.read(bs)) != -1) {
                bos.write(bs, 0, count);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Assert.assertArrayEquals(expBytes, bos.toByteArray());        
    }

    @Test
    public void testLoadContentForNotAttachment() throws IOException {
        
        when(configurationExplorer.isAttachmentType(NOT_ATTACHMENT_TYPE)).thenReturn(false);
        DomainObject domainObject = new GenericDomainObject();
        InputStream inputStream = null;
        ((GenericDomainObject) domainObject).setTypeName(NOT_ATTACHMENT_TYPE);
        try {
            inputStream = contentDao.loadContent(domainObject);
            Assert.fail("Exception shoul be thrown");
        } catch (DaoException ex) {
            Assert.assertEquals("Wrong exception is thrown", ex.getMessage(), "DomainObject is not attachment: "
                    + domainObject);
        }

    }
    
    @Test
    public void deleteContent() throws IOException {
        FileSystemAttachmentContentDaoImpl contentDao = new FileSystemAttachmentContentDaoImpl();
        contentDao.setAttachmentSaveLocation(TEST_OUT_DIR);
        byte[] expBytes = new byte[]{0, 1, 2, 3, 4, 5};
        String path = createAndCopyToFile(new ByteArrayInputStream(expBytes));
        Assert.assertTrue(new File(path).exists());
        DomainObject domainObject = new GenericDomainObject();
        String relPath = Paths.get(path).subpath(Paths.get(TEST_OUT_DIR).getNameCount(), Paths.get(path).getNameCount()).toString();
        domainObject.setValue(PATH_NAME, new StringValue(relPath));
        contentDao.deleteContent(domainObject);
        Assert.assertFalse(new File(path).exists());
    }

    private String createAndCopyToFile(InputStream inputStream) throws IOException {
        Path path = Paths.get(TEST_OUT_DIR, "FileSystemAttachmentContentDaoImplTest");
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        return path.toAbsolutePath().toString();
    }
}
