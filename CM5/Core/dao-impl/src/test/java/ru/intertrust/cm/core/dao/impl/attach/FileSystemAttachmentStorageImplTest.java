package ru.intertrust.cm.core.dao.impl.attach;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemAttachmentStorageImplTest {

    @Spy
    private ConfigurationExplorer confExplorer = loadConfiguration("config/attachment-storages-test.xml");

    @Mock
    private CurrentUserAccessor currentUserAccessor;
    @Mock
    private Environment env;
    @Mock
    private UserTransactionService txService;
    @Mock
    private FileTypeDetector fileTypeDetector;
    @Mock
    private FileDeleteStrategy fileDeleteStrategy;
    @Mock
    private DomainObject domainObject;
    @Mock
    private DeleteAttachmentStrategyFactory strategyFactory;

    private final String tmpDir = Paths.get(System.getProperty("java.io.tmpdir"),
            System.getProperty("attachment.test.dir", "CM5_Test")).toString();

    @Before
    public void init() throws IOException {
        Files.createDirectories(Paths.get(tmpDir));
    }

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
        AttachmentStorage.Context ctx = new AttachmentStorage.StaticContext()
                .attachmentType("MainAtt").parentObject(domainObject).fileName("test.txt").creationTime();
        when(fileTypeDetector.detectMimeType(anyString())).thenReturn("text/ok");
        try {
            byte[] content = "Test content".getBytes();
            AttachmentInfo info = testee.saveContent(new ByteArrayInputStream(content), ctx);
            assertEquals(content.length, (long) info.getContentLength());
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
        when(env.getProperty("attachments.storage.default.dir")).thenReturn(tmpDir);
        when(env.getProperty("attachments.storage.default.folders")).thenReturn("{doctype}/{year}-{month}/{ext}/{creator}");
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("default");
        AttachmentStorage.Context ctx = new AttachmentStorage.StaticContext()
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
        when(env.getProperty("attachments.storage.dir")).thenReturn(tmpDir);
        when(env.getProperty("attachments.storage.folders")).thenReturn("{Year}/{Month}/{DocType}");
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("Special");
        AttachmentStorage.Context ctx = new AttachmentStorage.StaticContext()
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
        when(env.getProperty("attachments.storage.alternate.dir")).thenReturn(tmpDir);
        when(env.getProperty("attachments.storage.alternate.folders")).thenReturn("{doctype}/{creator}");    //This must be ignored
        when(env.getProperty("${attachments.path.unixstyle:true}", boolean.class)).thenReturn(Boolean.TRUE);
        FileSystemAttachmentStorageImpl testee = createTestee("alternate");
        AttachmentStorage.Context ctx = new AttachmentStorage.StaticContext()
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
            String conf = new String(Files.readAllBytes(Paths.get(configUri)), StandardCharsets.UTF_8);
            ConfigurationClassesCache.getInstance().build();
            ConfigurationSerializer serializer = new ConfigurationSerializer();
            Configuration configuration = serializer.deserializeLoadedConfiguration(conf);
            ConfigurationExplorerImpl result =  new ConfigurationExplorerImpl(configuration);
            result.init();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private FileSystemAttachmentStorageImpl createTestee(String name) {
        AttachmentStorageConfig config = confExplorer.getConfig(AttachmentStorageConfig.class, name);
        FileSystemAttachmentStorageImpl testee = new FileSystemAttachmentStorageImpl(name,
                (FolderStorageConfig) config.getStorageConfig());
        when(strategyFactory.createDeleteStrategy(anyString(), any())).thenReturn(fileDeleteStrategy);
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
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) {
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
