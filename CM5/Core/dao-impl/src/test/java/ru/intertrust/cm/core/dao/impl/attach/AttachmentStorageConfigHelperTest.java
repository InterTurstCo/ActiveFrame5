package ru.intertrust.cm.core.dao.impl.attach;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.impl.attach.AttachmentStorageConfigHelper;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentStorageConfigHelperTest {

    private AttachmentStorageConfigHelper testee = withConfiguration("config/attachment-storages-test.xml");

    private AttachmentStorageConfigHelper withConfiguration(String path) {
        try {
            URI configUri = getClass().getClassLoader().getResource(path).toURI();
            String config = new String(Files.readAllBytes(Paths.get(configUri)), "UTF-8");
            ConfigurationClassesCache.getInstance().build();
            ConfigurationSerializer serializer = new ConfigurationSerializer();
            Configuration configuration = serializer.deserializeLoadedConfiguration(config);
            ConfigurationExplorerImpl confExplorer = new ConfigurationExplorerImpl(configuration);

            AttachmentStorageConfigHelper helper = new AttachmentStorageConfigHelper();
            Field field = AttachmentStorageConfigHelper.class.getDeclaredField("confExplorer");
            field.setAccessible(true);
            field.set(helper, confExplorer);
            return helper;
        } catch (Exception e) {
            throw new RuntimeException("Something gone wrong...", e);
        }
    }

    @Test
    public void testGetStorage_default() {
        String storageName = testee.getStorageForAttachment("MainAtt", "RootObject");
        assertEquals(AttachmentStorageConfigHelper.DEFAULT_STORAGE, storageName);
    }

    @Test
    public void testGetStorage_defined() {
        String storageName = testee.getStorageForAttachment("AltAtt", "RootObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_fromTemplate() {
        String storageName = testee.getStorageForAttachment("SpecAtt", "RootObject");
        assertEquals("Special", storageName);
    }

    @Test
    public void testGetStorage_defaultButOverridenInObject() {
        String storageName = testee.getStorageForAttachment("MainAtt", "ChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_definedButOverridenInObject() {
        String storageName = testee.getStorageForAttachment("AltAtt", "ChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_fromTemplateButOverridenInObject() {
        String storageName = testee.getStorageForAttachment("SpecAtt", "ChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_defaultButOverridenInParent() {
        String storageName = testee.getStorageForAttachment("MainAtt", "GrandChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_definedButOverridenInParent() {
        String storageName = testee.getStorageForAttachment("AltAtt", "GrandChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_fromTemplateButOverridenInParent() {
        String storageName = testee.getStorageForAttachment("SpecAtt", "GrandChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_fromParent() {
        String storageName = testee.getStorageForAttachment("TypeA_Att", "GreatGrandChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_definedOverridesParent() {
        String storageName = testee.getStorageForAttachment("TypeB_Att", "GreatGrandChildObject");
        assertEquals("Special", storageName);
    }

    @Test
    public void testGetStorage_fromTemplateOverridenInParent() {
        String storageName = testee.getStorageForAttachment("TypeC_Att", "GreatGrandChildObject");
        assertEquals("Alternate", storageName);
    }

    @Test
    public void testGetStorage_definedOverridesTemplate() {
        String storageName = testee.getStorageForAttachment("TypeD_Att", "GreatGrandChildObject");
        assertEquals("default", storageName);
    }
}
