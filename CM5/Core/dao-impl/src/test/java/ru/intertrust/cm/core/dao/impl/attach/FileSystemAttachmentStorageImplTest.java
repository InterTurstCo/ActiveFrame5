package ru.intertrust.cm.core.dao.impl.attach;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemAttachmentStorageImplTest {

    @Spy private ConfigurationExplorer confExplorer = loadConfiguration("config/attachment-storages-test.xml");

    @Mock private CurrentUserAccessor currentUserAccessor;
    @Mock private UserTransactionService txService;
    @Mock private Environment env;
    @Mock private ApplicationContext appContext;
    @Mock private FileTypeDetector fileTypeDetector;
    @Mock private FileDeleteStrategy fileDeleteStrategy;
    @Mock private DomainObject domainObject;

    private String tmpDir = Paths.get(System.getProperty("java.io.tmpdir"),
            System.getProperty("attachment.test.dir", "CM5_Test")).toString();

    @Test(expected = ConfigurationException.class)
    public void testInitialize_notConfigured() {
        @SuppressWarnings("unused")
        FileSystemAttachmentStorageImpl testee = createTestee("default");
        // Must not proceed creation
    }

    @Test
    public void testSaveContent_legacyLocationProperty() {
        when(env.getProperty(FileSystemAttachmentStorageImpl.PROP_LEGACY)).thenReturn(tmpDir);
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.FALSE);
        FileSystemAttachmentStorageImpl testee = createTestee("default");
        AttachmentStorage.Context ctx = new AttachmentStorage.Context()
                .attachmentType("MainAtt").parentObject(domainObject).fileName("test.txt").creationTime();
        when(fileTypeDetector.detectMimeType(anyString())).thenReturn("text/ok");
        try {
            byte[] content = "Test content".getBytes();
            AttachmentInfo info = testee.saveContent(new ByteArrayInputStream(content), ctx);
            assertTrue(content.length == info.getContentLength());
            assertEquals("text/ok", info.getMimeType());
            String requiredPath = new SimpleDateFormat("yyyy/MM/dd/").format(ctx.getCreationTime().getTime())
                    .replaceAll("\\/", "\\" + File.separator);
            assertTrue(info.getRelativePath().startsWith(requiredPath));
            assertTrue(info.getRelativePath().endsWith(".txt"));
        } finally {
            clearTestDir();
        }
    }

    @Test
    public void testSaveContent_configured() {
        when(env.getProperty("attachment.storage.default.root")).thenReturn(tmpDir);
        when(env.getProperty("attachment.storage.default.folders")).thenReturn("{doctype}/{year}-{month}/{ext}/{creator}");
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("default");
        AttachmentStorage.Context ctx = new AttachmentStorage.Context()
                .attachmentType("MainAtt").parentObject(domainObject).fileName("test.txt").creationTime();
        when(domainObject.getTypeName()).thenReturn("RootObject");
        when(currentUserAccessor.getCurrentUserId()).thenReturn(new RdbmsId(11, 101));
        try {
            byte[] content = "Test content".getBytes();
            AttachmentInfo info = testee.saveContent(new ByteArrayInputStream(content), ctx);
            assertTrue(info.getRelativePath().startsWith("RootObject/"
                    + new SimpleDateFormat("yyyy-MM").format(ctx.getCreationTime().getTime()) + "/txt/101/"));
        } finally {
            clearTestDir();
        }
    }

    @Test
    public void testSaveContent_globallyConfigured() {
        when(env.getProperty("attachment.storage.root")).thenReturn(tmpDir);
        when(env.getProperty("attachment.storage.folders")).thenReturn("{Year}/{Month}/{DocType}");
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("Special");
        AttachmentStorage.Context ctx = new AttachmentStorage.Context()
                .attachmentType("SpecAtt").parentObject(domainObject).fileName("original name.ext").creationTime();
        when(domainObject.getTypeName()).thenReturn("RootObject");
        try {
            byte[] content = "Test content".getBytes();
            AttachmentInfo info = testee.saveContent(new ByteArrayInputStream(content), ctx);
            assertTrue(info.getRelativePath().startsWith(
                    new SimpleDateFormat("yyyy/MM").format(ctx.getCreationTime().getTime()) + "/RootObject/"));
            assertFalse(info.getRelativePath().contains("original name"));
        } finally {
            clearTestDir();
        }
    }

    @Test
    public void testSaveContent_foldersConfigurationIgnored() {
        when(env.getProperty("attachment.storage.alternate.root")).thenReturn(tmpDir);
        when(env.getProperty("attachment.storage.alternate.folders")).thenReturn("{doctype}/{creator}");    //This must be ignored
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("alternate");
        AttachmentStorage.Context ctx = new AttachmentStorage.Context()
                .attachmentType("AltAtt").parentObject(domainObject).fileName("test.txt").creationTime();
        when(domainObject.getTypeName()).thenReturn("RootObject");
        try {
            byte[] content = "Test content".getBytes();
            AttachmentInfo info = testee.saveContent(new ByteArrayInputStream(content), ctx);
            assertTrue(info.getRelativePath().startsWith(
                    new SimpleDateFormat("yyyy").format(ctx.getCreationTime().getTime()) + "/RootObject/"));
                    // actual format is taken from configuration
        } finally {
            clearTestDir();
        }
    }

    private ConfigurationExplorer loadConfiguration(String path) {
        try {
            URI configUri = getClass().getClassLoader().getResource(path).toURI();
            String conf = new String(Files.readAllBytes(Paths.get(configUri)), "UTF-8");
            ConfigurationClassesCache.getInstance().build();
            ConfigurationSerializer serializer = new ConfigurationSerializer();
            Configuration configuration = serializer.deserializeLoadedConfiguration(conf);
            return new ConfigurationExplorerImpl(configuration);
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private FileSystemAttachmentStorageImpl createTestee(String name) {
        AttachmentStorageConfig config = confExplorer.getConfig(AttachmentStorageConfig.class, name);
        FileSystemAttachmentStorageImpl testee = new FileSystemAttachmentStorageImpl(name,
                (FolderStorageConfig) config.getStorageConfig());
        when(appContext.getBean(anyString(), eq(FileDeleteStrategy.class))).thenReturn(fileDeleteStrategy);
        injectMocks(testee);
        testee.initialize();
        return testee;
    }

    private void clearTestDir() {
        try {
            Files.walkFileTree(Paths.get(tmpDir), new FileVisitor<Path>() {

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
                    System.out.println("Failed to open file " + file);
                    return FileVisitResult.TERMINATE;
                }
            });
        } catch (IOException e) {
            System.out.println("Failed to clean temporary folder: " + e.getMessage());
        }
    }

    private void injectMocks(Object target) {
        Map<String, Object> mocks = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getAnnotation(Mock.class) != null || field.getAnnotation(Spy.class) != null) {
                try {
                    mocks.put(field.getType().getName(), field.get(this));
                } catch (Exception e) {
                    System.out.println("Error accessing field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Autowired.class) != null) {
                Object mock = mocks.get(field.getType().getName());
                if (mock == null) {
                    System.out.println("Couldn't find mock for field " + field.getName() + " [" + field.getType().getName() + "]");
                    continue;
                }
                field.setAccessible(true);
                try {
                    field.set(target, mock);
                } catch (Exception e) {
                    System.out.println("Error initializing field " + field.getName() + ": " + e.getMessage());
                }
            }
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                field.setAccessible(true);
                try {
                    field.set(target, env.getProperty(value.value(), field.getType()));
                } catch (Exception e) {
                    System.out.println("Error initializing field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
