package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 6:32 PM
 */
public class ConfigurationSerializerTest {

    private static final String CONFIGURATION_SCHEMA_PATH = "test-config/configuration-test.xsd";
    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects-test.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections-test.xml";

    private static final String DESERIALIZED_CONFIGURATION_PATH = "test-config/deserialized-configuration-test.xml";
    private static final String INVALID_DESERIALIZED_CONFIGURATION_PATH =
            "test-config/deserialized-configuration-invalid-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDeserializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.serializeConfiguration();
        String testDeserializedConfiguration = ConfigurationSerializer.deserializeConfiguration(configuration);

        String deserializedConfiguration = readTextFile(DESERIALIZED_CONFIGURATION_PATH).replaceAll("\r\n", "\n");
        assertEquals(testDeserializedConfiguration, deserializedConfiguration);
    }

    @Test
    public void testDeserializeNullConfiguration() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Failed to deserialize configuration");

        ConfigurationSerializer.deserializeConfiguration(null);
    }

    @Test
    public void testSerializeTrustedConfiguration() throws Exception {
        String deserializedConfiguration = readTextFile(DESERIALIZED_CONFIGURATION_PATH);
        Configuration testConfiguration =
                ConfigurationSerializer.serializeTrustedConfiguration(deserializedConfiguration);
        assertNotNull(testConfiguration);

        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.serializeConfiguration();

        assertEquals(testConfiguration, configuration);
    }

    @Test
    public void testSerializeTrustedConfigurationInvalid() throws Exception {
        String deserializedConfiguration = readTextFile(INVALID_DESERIALIZED_CONFIGURATION_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Failed to serialize configuration from String");

        ConfigurationSerializer.serializeTrustedConfiguration(deserializedConfiguration);
    }

    @Test
     public void testSerializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.serializeConfiguration();

        assertNotNull(configuration);

        List configurationList = configuration.getConfigurationList();
        assertNotNull(configurationList);
        assertEquals(configurationList.size(), 6);

        List<String> configurationNames = new ArrayList<>();
        configurationNames.addAll(Arrays.asList("Employees", "Employees_2", "Outgoing_Document", "Person",
                "Employee", "Department"));

        for (Object configurationItem : configurationList) {
            String name = DomainObjectTypeConfig.class.equals(configurationItem.getClass()) ?
                    ((DomainObjectTypeConfig) configurationItem).getName() :
                    ((CollectionConfig) configurationItem).getName();
            assertTrue(configurationNames.contains(name));
            configurationNames.remove(name);
        }
    }

    private ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH));
        configurationSerializer.setConfigurationFilePaths(configPaths);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        return configurationSerializer;
    }

    private String readTextFile(String filePath) throws IOException {
       File file = new File(FileUtils.getFileURL(filePath).getFile());

        FileInputStream fin = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        new DataInputStream(fin).readFully(buffer);
        fin.close();

        return Charset.forName("UTF-8").decode(ByteBuffer.wrap(buffer)).toString();
    }
}
