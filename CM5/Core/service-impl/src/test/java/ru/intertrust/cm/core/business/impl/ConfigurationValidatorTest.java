package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.Configuration;

/**
 * Тестовый класс для {@link ru.intertrust.cm.core.business.impl.ConfigurationValidator}
 * @author atsvetkov
 *
 */
public class ConfigurationValidatorTest {

    private static final String CONFIGURATION_SCHEMA = "test-config/configuration.xsd";
    private static final String CONFIGURATION_VALID = "test-config/business-objects.xml";
    private static final String CONFIGURATION_INVALID = "test-config/business-objects-invalid.xml";
    private static final String CONFIGURATION_INVALID_REFERENCE = "test-config/business-objects-invalid-reference.xml";
    private static final String COLLECTIONS_CONFIGURATION_VALID = "test-config/collections.xml";

    
    private ConfigurationValidator configurationValidator;
    private ConfigurationLoader configurationLoader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        configurationValidator = new ConfigurationValidator();
        configurationValidator.setCollectionsConfigurationPath(COLLECTIONS_CONFIGURATION_VALID);        

        configurationLoader = new ConfigurationLoader();

    }

    @Test
    public void testValidate() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath, Configuration.class);

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        configurationValidator.validate();

    }

    @Test
    public void testInvalidAgainstXSD() throws Exception {
        String configurationPath = CONFIGURATION_INVALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath, Configuration.class);
        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("is not valid against XSD schema");

        configurationValidator.validate();
    }

    @Test
    public void testInvalidReference() throws Exception {
        String configurationPath = CONFIGURATION_INVALID_REFERENCE;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath, Configuration.class);
        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("BusinessObjectConfiguration is not found for name");
        configurationValidator.validate();
    }

    @Test
    public void testNullConfigurationPath() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath, Configuration.class);
        configurationValidator.setConfigurationPath(null);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please set the configurationPath for ConfigurationValidator before validating");
        configurationValidator.validate();

    }

    @Test
    public void testNullConfiguration() throws Exception {
        String configurationPath = CONFIGURATION_VALID;

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(null);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please set the configuration object");
        configurationValidator.validate();

    }

    @Test
    public void testNullConfigurationSchema() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath, Configuration.class);

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(null);
        configurationValidator.setConfiguration(configuration);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please set the configurationSchemaPath");
        configurationValidator.validate();

    }
}
