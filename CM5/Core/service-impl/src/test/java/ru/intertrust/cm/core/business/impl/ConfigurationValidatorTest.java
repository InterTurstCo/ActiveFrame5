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

    private static final String CONFIGURATION_SCHEMA = "config/business-objects.xsd";
    private static final String CONFIGURATION_VALID = "config/business-objects.xml";
    private static final String CONFIGURATION_INVALID = "config/business-objects-invalid.xml";
    private static final String CONFIGURATION_INVALID_REFERENCE = "config/business-objects-invalid-reference.xml";

    private ConfigurationValidator configurationValidator;
    private ConfigurationLoader configurationLoader;

    @Rule
    public ExpectedException expectedExeption = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        configurationValidator = new ConfigurationValidator();

        configurationLoader = new ConfigurationLoader();

    }

    @Test
    public void testValidate() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath);

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        configurationValidator.validate();

    }

    @Test
    public void testInvalidAgainstXSD() throws Exception {
        String configurationPath = CONFIGURATION_INVALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath);
        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        expectedExeption.expect(RuntimeException.class);
        expectedExeption.expectMessage("is not valid against XSD schema");

        configurationValidator.validate();
    }

    @Test
    public void testInvalidReference() throws Exception {
        String configurationPath = CONFIGURATION_INVALID_REFERENCE;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath);
        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);

        expectedExeption.expect(RuntimeException.class);
        expectedExeption.expectMessage("BusinessObjectConfiguration is not found for name");
        configurationValidator.validate();
    }

    @Test
    public void testNullConfigurationPath() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath);
        configurationValidator.setConfigurationPath(null);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(configuration);
        expectedExeption.expect(RuntimeException.class);
        expectedExeption.expectMessage("Please set the configurationPath for ConfigurationValidator before validating");
        configurationValidator.validate();

    }

    @Test
    public void testNullConfiguration() throws Exception {
        String configurationPath = CONFIGURATION_VALID;

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(CONFIGURATION_SCHEMA);
        configurationValidator.setConfiguration(null);
        expectedExeption.expect(RuntimeException.class);
        expectedExeption.expectMessage("Please set the configuration object");
        configurationValidator.validate();

    }

    @Test
    public void testNullConfigurationSchema() throws Exception {
        String configurationPath = CONFIGURATION_VALID;
        Configuration configuration = configurationLoader.serializeConfiguration(configurationPath);

        configurationValidator.setConfigurationPath(configurationPath);
        configurationValidator.setConfigurationSchemaPath(null);
        configurationValidator.setConfiguration(configuration);
        expectedExeption.expect(RuntimeException.class);
        expectedExeption.expectMessage("Please set the configurationSchemaPath");
        configurationValidator.validate();

    }
}
