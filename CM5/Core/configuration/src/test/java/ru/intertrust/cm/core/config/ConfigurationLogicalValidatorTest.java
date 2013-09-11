package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.model.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 5:21 PM
 */
public class ConfigurationLogicalValidatorTest {

    private static final String DOMAIN_OBJECTS_INVALID_EXTENDS_ATTRIBUTE_CONFIG_PATH =
            "config/domain-objects-invalid-extends-attribute-test.xml";

    private static final String DOMAIN_OBJECTS_INVALID_PARENT_CONFIG_PATH =
            "config/domain-objects-invalid-parent-test.xml";

    private static final String DOMAIN_OBJECTS_INVALID_REFERENCE_CONFIG_PATH =
            "config/domain-objects-invalid-reference-field-test.xml";

    private static final String DOMAIN_OBJECTS_INVALID_UNIQUE_KEY_CONFIG_PATH =
            "config/domain-objects-invalid-unique-key-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(DOMAIN_OBJECTS_CONFIG_PATH);
    }

    @Test
    public void testValidateInvalidExtendsAttribute() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Extended DomainObject Configuration is not found for name 'Person'");

        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_EXTENDS_ATTRIBUTE_CONFIG_PATH);
    }

    @Test
    public void testValidateInvalidParentAttribute() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Parent DomainObject Configuration is not found for name 'Person'");

        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_PARENT_CONFIG_PATH);
    }

    @Test
    public void testValidateInvalidReference() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Definition is not found for 'Employee' referenced from 'Incoming_Document2'");

        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_REFERENCE_CONFIG_PATH);
    }

    @Test
    public void testValidateInvalidUniqueKey() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("FieldConfig with name 'Invalid_field' is not found in domain object " +
                "'Outgoing_Document'");

        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_UNIQUE_KEY_CONFIG_PATH);
    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH));

        File file = new File (configPath);
        System.out.println(file.getAbsolutePath()) ;
        System.out.println(file.exists()) ;

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        System.out.println( ConfigurationSerializer.serializeConfiguration(configuration) );
        return new ConfigurationExplorerImpl(configuration);
    }
}
