package ru.intertrust.cm.core.business.impl;

import static ru.intertrust.cm.core.business.impl.ConfigurationHelper.findBusinessObjectConfigByName;
import static ru.intertrust.cm.core.business.impl.ConfigurationHelper.findFieldConfigForBusinessObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;

/**
 * Валидирует конфигурацию бизнес-объектов на предмет соответствия XSD схеме и логически. 
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class ConfigurationValidator {

    private String configurationPath;

    private String collectionsConfigurationPath;

    private String configurationSchemaPath;
        
    private Configuration configuration;

    /**
     * Конструктор по умолчанию. Нужен для инициализации этого класса через Spring
     */
    public ConfigurationValidator() {
    }

    /**
     * Возвращает путь к конфигурационному файлу с бизнес-объектами.
     * @return
     */
    public String getConfigurationPath() {
        return configurationPath;
    }

    /**
     * Устанавливает путь к конфигурационному файлу с бизнес-объектами. Нужен для валидации по XSD схеме.
     * @param configurationPath
     */
    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    /**
     * Устанавливает путь к конфигурационному файлу коллекций. Нужен для валидации по XSD схеме.
     * @param collectionsConfigurationPath
     */
    public void setCollectionsConfigurationPath(String collectionsConfigurationPath) {
        this.collectionsConfigurationPath = collectionsConfigurationPath;
    }

    /**
     * Возвращает путь к файлу с XSD схемой.
     * @return
     */
    public String getConfigurationSchemaPath() {
        return configurationSchemaPath;
    }

    /**
     * Устанавливает путь к файлу с XSD схемой.
     * @param configurationSchemaPath
     */
    public void setConfigurationSchemaPath(String configurationSchemaPath) {
        this.configurationSchemaPath = configurationSchemaPath;
    }

    /**
     * Возвращает сериализованную конфигурацию.
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Устанавливает сериализованную конфигурацию. Необходима для логической валидациии конфигурации.
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Выполняет валидацию конфигурации на предмет соответствия XSD схеме и логическую валидацию.
     */
    public void validate() {
        if (configurationPath == null) {
            throw new RuntimeException("Please set the configurationPath for ConfigurationValidator before validating");
        }
        if (collectionsConfigurationPath == null) {
            throw new RuntimeException("Please set the collectionsConfigurationPath for ConfigurationValidator before validating");
        }

        if (configuration == null) {
            throw new RuntimeException("Please set the configuration object for ConfigurationValidator before validating");
        }
        validateAgainstXSD();
        validateLogically();
    }

    private void validateAgainstXSD() {
        if (configurationSchemaPath == null) {
            throw new RuntimeException("Please set the configurationSchemaPath for ConfigurationValidator before validating");
        }        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            Source schemaSource = new StreamSource(getResourceAsStream(configurationSchemaPath));

            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            
            validateBusinessObjectConfiguration(validator);
            validateCollectionsConfiguration(validator);
            // TODO Log success information using logging API
            System.out.println("Document is valid against XSD");
        } catch (SAXException ex) {
            throw new RuntimeException("Document " + configurationSchemaPath + " is not valid against XSD schema: " + ex.getMessage(), ex);
        } catch (IOException e) {
            throw new RuntimeException(" File " + configurationPath + " not found. " + e.getMessage(), e);

        }
    }

    private void validateBusinessObjectConfiguration(Validator validator) throws SAXException, IOException {
        validator.setErrorHandler(new ValidationErrorHandler());

        InputStream configurationInputStream = getResourceAsStream(configurationPath);
        Source source = new StreamSource(configurationInputStream);
        validator.validate(source);
    }

    private void validateCollectionsConfiguration(Validator validator) throws SAXException, IOException {
        validator.setErrorHandler(new ValidationErrorHandler());
        
        InputStream configurationInputStream = getResourceAsStream(collectionsConfigurationPath);
        Source source = new StreamSource(configurationInputStream);
        validator.validate(source);
    }

    protected InputStream getResourceAsStream(String resourcePath) {
        return FileUtils.getFileInputStream(resourcePath);
    }   

    private void validateLogically() {
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
     * Обработчик ошибок валидации на предмет соответствия XSD схеме. Позволяет по различному обрабатывать ошибки разной критичности.
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
