package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.model.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.*;
import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 6:32 PM
 */
public class ConfigurationSerializerTest {

    private static final String ACCESS_CONFIG_PATH = "config/access-test.xml";
    private static final String SERIALIZED_CONFIGURATION_PATH = "config/serialized-configuration-test.xml";
    private static final String INVALID_SERIALIZED_CONFIGURATION_PATH =
            "config/serialized-configuration-invalid-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testSerializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.deserializeConfiguration();
        String serializedConfiguration = ConfigurationSerializer.serializeConfiguration(configuration);

        String expectedSerializedConfiguration = readTextFile(SERIALIZED_CONFIGURATION_PATH).replaceAll("\r\n", "\n");
        assertEquals(expectedSerializedConfiguration, serializedConfiguration);
    }

    @Test
    public void testSerializeNullConfiguration() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Failed to deserialize configuration");

        ConfigurationSerializer.serializeConfiguration(null);
    }

    @Test
    public void testDeserializeTrustedConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        String deserializedConfiguration = readTextFile(SERIALIZED_CONFIGURATION_PATH);
        Configuration testConfiguration =
                configurationSerializer.deserializeTrustedConfiguration(deserializedConfiguration);
        assertNotNull(testConfiguration);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        assertEquals(testConfiguration, configuration);
    }

    @Test
    public void testDeserializeTrustedConfigurationInvalid() throws Exception {
        String serializedConfiguration = readTextFile(INVALID_SERIALIZED_CONFIGURATION_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Failed to serialize configuration from String");

        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        configurationSerializer.deserializeTrustedConfiguration(serializedConfiguration);
    }

    @Test
     public void testDeserializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.deserializeConfiguration();

        assertNotNull(configuration);

        List configurationList = configuration.getConfigurationList();
        assertNotNull(configurationList);
        assertEquals(9, configurationList.size());

        List<String> configurationNames = new ArrayList<>();
        configurationNames.addAll(Arrays.asList("Employees", "Employees_2", "Outgoing_Document", "Person",
                "Assignment", "Employee", "Department", "Incoming_Document", "Incoming_Document2"));

        for (Object configurationItem : configurationList) {
            String name = DomainObjectTypeConfig.class.equals(configurationItem.getClass()) ?
                    ((DomainObjectTypeConfig) configurationItem).getName() :
                    ((CollectionConfig) configurationItem).getName();
            assertTrue(configurationNames.contains(name));
            configurationNames.remove(name);
        }
    }


    @Test
    public void testDeserializeAccessConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(ACCESS_CONFIG_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        assertNotNull(configuration);
        List configurationList = configuration.getConfigurationList();

        for (Object configurationItem : configurationList) {
            if (StaticGroupConfig.class.equals(configurationItem.getClass())) {
                assertNotNull(((StaticGroupConfig) configurationItem).getName());
                assertEquals("Администраторы", ((StaticGroupConfig) configurationItem).getName());
            } else if (DynamicGroupConfig.class.equals(configurationItem.getClass())) {
                assertNotNull(((DynamicGroupConfig) configurationItem).getName());
            } else if (ContextRoleConfig.class.equals(configurationItem.getClass())) {
                assertNotNull(((ContextRoleConfig) configurationItem).getName());
            } else if (AccessMatrixConfig.class.equals(configurationItem.getClass())) {
                assertNotNull(((AccessMatrixConfig) configurationItem).getType());
            }

        }
    }
    
    static ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths =
                new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

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
