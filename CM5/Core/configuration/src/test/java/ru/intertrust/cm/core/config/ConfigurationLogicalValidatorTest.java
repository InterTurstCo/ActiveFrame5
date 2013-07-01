package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.model.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 5:21 PM
 */
public class ConfigurationLogicalValidatorTest {

    private static final String CONFIGURATION_SCHEMA_PATH = "test-config/configuration-test.xsd";
    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects-test.xml";
    private static final String DOMAIN_OBJECTS_INVALID_PARENT_CONFIG_PATH =
            "test-config/domain-objects-invalid-parent-test.xml";
    private static final String DOMAIN_OBJECTS_INVALID_REFERENCE_CONFIG_PATH =
            "test-config/domain-objects-invalid-reference-field-test.xml";
    private static final String DOMAIN_OBJECTS_INVALID_UNIQUE_KEY_CONFIG_PATH =
            "test-config/domain-objects-invalid-unique-key-test.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(DOMAIN_OBJECTS_CONFIG_PATH);
        configurationExplorer.build();
    }

    @Test
    public void testValidateInvalidParent() throws Exception {
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_PARENT_CONFIG_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Parent DomainObject Configuration is not found for name 'Person'");

        configurationExplorer.build();

    }

    @Test
    public void testValidateInvalidReference() throws Exception {
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_REFERENCE_CONFIG_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Referenced DomainObject Configuration is not found for name 'Employee'");

        configurationExplorer.build();

    }

    @Test
    public void testValidateInvalidUniqueKey() throws Exception {
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(DOMAIN_OBJECTS_INVALID_UNIQUE_KEY_CONFIG_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("FieldConfig with name 'Invalid field' is not found in domain object 'Outgoing Document'");

        configurationExplorer.build();

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths =
                new HashSet<String>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH));
        configurationSerializer.setConfigurationFilePaths(configPaths);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.serializeConfiguration();
        return new ConfigurationExplorerImpl(configuration);
    }
}
