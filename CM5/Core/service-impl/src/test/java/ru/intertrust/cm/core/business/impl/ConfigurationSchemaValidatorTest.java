package ru.intertrust.cm.core.business.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Тестовый класс для {@link BusinessObjectsConfigurationLogicalValidator}
 * @author atsvetkov
 *
 */
public class ConfigurationSchemaValidatorTest {

    private static final String CONFIGURATION_SCHEMA = "test-config/configuration.xsd";
    private static final String CONFIGURATION_VALID = "test-config/business-objects.xml";
    private static final String CONFIGURATION_INVALID = "test-config/business-objects-invalid.xml";
    private static final String CONFIGURATION_INVALID_REFERENCE = "test-config/business-objects-invalid-reference.xml";
    private static final String COLLECTIONS_CONFIGURATION_VALID = "test-config/collections.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(CONFIGURATION_VALID, CONFIGURATION_SCHEMA);
        validator.validate();
    }

    @Test
    public void testValidateCollectionsConfiguration() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(COLLECTIONS_CONFIGURATION_VALID, CONFIGURATION_SCHEMA);
        validator.validate();
    }

    @Test
    public void testInvalidAgainstXSD() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(CONFIGURATION_INVALID, CONFIGURATION_SCHEMA);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("is not valid against XSD schema");

        validator.validate();
    }

    @Test
    public void testInvalidReference() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(CONFIGURATION_INVALID_REFERENCE, CONFIGURATION_SCHEMA);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("File not found for path '" + CONFIGURATION_INVALID_REFERENCE + "'");

        validator.validate();
    }

    @Test
    public void testNullConfigurationPath() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(null, CONFIGURATION_SCHEMA);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please set the configurationPath for ConfigurationSchemaValidator before validating");

        validator.validate();
    }

    @Test
    public void testNullConfigurationSchema() throws Exception {
        ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(CONFIGURATION_VALID, null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please set the configurationSchemaPath");

        validator.validate();
    }
}
