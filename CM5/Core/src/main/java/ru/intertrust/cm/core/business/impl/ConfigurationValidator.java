package ru.intertrust.cm.core.business.impl;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ru.intertrust.cm.core.config.*;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static ru.intertrust.cm.core.business.impl.ConfigurationHelper.findBusinessObjectConfigByName;
import static ru.intertrust.cm.core.business.impl.ConfigurationHelper.findFieldConfigForBusinessObject;

/**
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class ConfigurationValidator {

    private String configurationPath;

    private String configurationSchemaPath;

    private Configuration configuration;

    public ConfigurationValidator() {
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public String getConfigurationSchemaPath() {
        return configurationSchemaPath;
    }

    public void setConfigurationSchemaPath(String configurationSchemaPath) {
        this.configurationSchemaPath = configurationSchemaPath;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void validate() {
        validateAgainstXSD();
        validateLogically();
    }

    public void validateAgainstXSD() {
        if (configurationPath == null) {
            throw new RuntimeException("Please set the configurationPath for ConfigurationValidator before validating");
        }
        if (configurationSchemaPath == null) {
            throw new RuntimeException(
                    "Please set the configurationSchemaPath for ConfigurationValidator before validating");
        }

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File configurationSchemaFile = new File(configurationSchemaPath);
        try {

            Schema schema = factory.newSchema(configurationSchemaFile);
            Validator validator = schema.newValidator();
            Source source = new StreamSource(configurationPath);
            validator.setErrorHandler(new ValidationErrorHandler());
            validator.validate(source);
            // TODO Log success information using logging API
            System.out.println("Document is valid against XSD");

        } catch (SAXException ex) {
            throw new RuntimeException("Document " + configurationSchemaFile.getName() + " is not valid: "
                    + ex.getMessage(), ex);
        } catch (IOException e) {
            throw new RuntimeException(" File " + configurationPath + " not found. " + e.getMessage(), e);

        }
    }

    public void validateLogically() {
        if (configuration == null) {
            throw new RuntimeException(
                    "Please set the configuration object for ConfigurationValidator before validating");
        }
        List<BusinessObjectConfig> businessObjectConfigs = configuration.getBusinessObjectConfigs();
        if (businessObjectConfigs.isEmpty()) {
            return;
        }
        for (BusinessObjectConfig businessObjectConfig : businessObjectConfigs) {
            validateBusinessObjectConfig(businessObjectConfig);
        }
        // TODO Log success information using logging API
        System.out.println("Document has passed logical validation");
    }

    private void validateBusinessObjectConfig(BusinessObjectConfig businessObjectConfig) {
        if (businessObjectConfig == null) {
            return;
        }

        validateParentConfig(businessObjectConfig);
        validateReferenceFields(businessObjectConfig);
        validateUniqueKeys(businessObjectConfig);
    }

    private void validateUniqueKeys(BusinessObjectConfig businessObjectConfig) {
        for (UniqueKeyConfig uniqueKeyConfig : businessObjectConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                findFieldConfigForBusinessObject(businessObjectConfig, uniqueKeyFieldConfig.getName());
            }
        }
    }

    private void validateReferenceFields(BusinessObjectConfig businessObjectConfig) {
        for (FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                findBusinessObjectConfigByName(configuration, ((ReferenceFieldConfig) fieldConfig).getType());
            }
        }
    }

    private void validateParentConfig(BusinessObjectConfig businessObjectConfig) {
        String parentConfig = businessObjectConfig.getParentConfig();
        if (parentConfig != null) {
            findBusinessObjectConfigByName(configuration, parentConfig);
        }
    }

    /**
     * Gives the possibility to handle differently validation errors and warnings.
     * 
     * @author atsvetkov
     * 
     */
    private static class ValidationErrorHandler implements ErrorHandler {

        public void warning(SAXParseException ex) {
            // TODO Log warnings using logging API
            System.err.println(ex.getMessage());
        }

        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }
    }

}
