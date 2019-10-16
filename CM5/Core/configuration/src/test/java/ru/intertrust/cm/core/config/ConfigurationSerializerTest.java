package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testSerializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.deserializeConfiguration();
        String serializedConfiguration = ConfigurationSerializer.serializeConfiguration(configuration);

        String expectedSerializedConfiguration = readTextFile(SERIALIZED_CONFIGURATION_PATH).replaceAll("\r\n", "\n");
        assertEquals(expectedSerializedConfiguration, serializedConfiguration);
        
        //повторная десериализация сгенеренного xml (так как на данном этапе expectedSerializedConfiguration и serializedConfiguration идентичны то на вход десериализатора передаем файл SERIALIZED_CONFIGURATION_PATH)
        configurationSerializer = createConfigurationSerializer(SERIALIZED_CONFIGURATION_PATH);
        configuration = configurationSerializer.deserializeConfiguration();
        
    }

    @Test
    public void testTabTemplatesSerialization() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(FORM_TEMPLATES_CONFIG);
        Configuration configuration = configurationSerializer.deserializeConfiguration();
        String serializedConfiguration = ConfigurationSerializer.serializeConfiguration(configuration);
        assertTrue(serializedConfiguration.contains("<template-based-tab template"));
    }

    @Test
    public void testSerializeNullConfiguration() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Failed to serialize configuration");

        ConfigurationSerializer.serializeConfiguration(null);
    }

    @Test
    public void testDeserializeLoadedConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        String deserializedConfiguration = readTextFile(SERIALIZED_CONFIGURATION_PATH);
        Configuration testConfiguration =
                configurationSerializer.deserializeLoadedConfiguration(deserializedConfiguration);
        assertNotNull(testConfiguration);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        assertEquals(configuration, testConfiguration);
    }

    @Test
    public void testDeserializeLoadedConfigurationInvalid() throws Exception {
        String serializedConfiguration = readTextFile(INVALID_SERIALIZED_CONFIGURATION_PATH);
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration conf = configurationSerializer.deserializeLoadedConfiguration(serializedConfiguration);
        assertNotNull(conf);
    }

    @Test
     public void testDeserializeConfiguration() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);
        Configuration configuration = configurationSerializer.deserializeConfiguration();

        assertNotNull(configuration);

        List configurationList = configuration.getConfigurationList();
        assertNotNull(configurationList);
        assertEquals(35, configurationList.size());

        List<String> configurationNames = new ArrayList<>();
        configurationNames.addAll(Arrays.asList("Employees", "Employees_2", "Outgoing_Document", "Person",
                "Assignment", "Employee", "Department", "Incoming_Document", "Incoming_Document2",
                "Authentication_Info", "User_Group", "Group_Member", "Group_Admin", "Delegation", "Organization",
                "A1", "B1", "C1", "D1", "Test_DO_1", "Test_DO_2", "Test_DO_3", "Test_DO_4", "Test_DO_5",
                "Ref_DO_3_1", "Ref_DO_3_2", "Ref_DO_1", "Ref_DO_2", "Ref_DO_3", "Test_type_1", "Test_type_2",
                "Test_type_3", "Test_type_4", "Test_type_5"));

        for (Object configurationItem : configurationList) {
            if (configurationItem.getClass().equals(GlobalSettingsConfig.class)) {
                continue;
            }
            String name = null;
            if(configurationItem instanceof DomainObjectTypeConfig){
                name = ((DomainObjectTypeConfig) configurationItem).getName();
            }else if(configurationItem instanceof CollectionConfig){
                name = ((CollectionConfig) configurationItem).getName();
            }
            
            if(name!= null){
                assertTrue(configurationNames.contains(name));
                configurationNames.remove(name);
                
            }
        }
    }


    //@Test
    //TODO восстановить тест после окончания изменения конфигурации прав
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

    private ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(configPath));

        return configurationSerializer;
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        URL moduleUrl = getClass().getClassLoader().getResource(".");

        ModuleService result = new ModuleService();
        ModuleConfiguration confCore = new ModuleConfiguration();
        confCore.setName("core");
        result.getModuleList().add(confCore);
        confCore.setConfigurationPaths(new ArrayList<String>());
        confCore.getConfigurationPaths().add(configPath);
        confCore.getConfigurationPaths().add(COLLECTIONS_CONFIG_PATH);
        confCore.getConfigurationPaths().add(GLOBAL_XML_PATH);
        confCore.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        confCore.setModuleUrl(moduleUrl);

        ModuleConfiguration confCustom = new ModuleConfiguration();
        confCustom.setName("custom");
        result.getModuleList().add(confCustom);
        confCustom.setConfigurationPaths(new ArrayList<String>());
        confCustom.getConfigurationPaths().add(MODULES_CUSTOM_CONFIG);
        confCustom.getConfigurationPaths().add(MODULES_DOMAIN_OBJECTS);
        confCustom.setConfigurationSchemaPath(MODULES_CUSTOM_SCHEMA);
        confCustom.setDepends(new ArrayList<String>());
        confCustom.getDepends().add(confCore.getName());
        confCustom.setModuleUrl(moduleUrl);

        return result;
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
