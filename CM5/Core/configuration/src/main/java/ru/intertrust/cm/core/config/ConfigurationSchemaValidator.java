package ru.intertrust.cm.core.config;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

import static ru.intertrust.cm.core.config.FileUtils.getFileInputStream;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 6:13 PM
 */
public class ConfigurationSchemaValidator {

    private String configurationPath;
    private String configurationSchemaPath;

    public ConfigurationSchemaValidator(String configurationPath, String configurationSchemaPath) {
        this.configurationPath = configurationPath;
        this.configurationSchemaPath = configurationSchemaPath;
    }

    /**
     * Возвращает путь к конфигурационному файлу с доменными объектами.
     * @return
     */
    public String getConfigurationPath() {
        return configurationPath;
    }

    /**
     * Устанавливает путь к конфигурационному файлу с доменными объектами. Нужен для валидации по XSD схеме.
     * @param configurationPath
     */
    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
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
     * Выполняет валидацию конфигурации на предмет соответствия XSD схеме и логическую валидацию.
     */
    public void validate() {
        if (configurationPath == null) {
            throw new RuntimeException("Please set the configurationPath for ConfigurationSchemaValidator before validating");
        }
        validateAgainstXSD();
    }

    private void validateAgainstXSD() {
        if (configurationSchemaPath == null) {
            throw new RuntimeException("Please set the configurationSchemaPath for DomainObjectsConfigurationLogicalValidator before validating");
        }
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            Source schemaSource = new StreamSource(getFileInputStream(configurationSchemaPath));

            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();

            validateDomainObjectConfiguration(validator);
            // TODO Log success information using logging API
            System.out.println("Document is valid against XSD");
        } catch (SAXException ex) {
            throw new RuntimeException("Document " + configurationSchemaPath + " is not valid against XSD schema: " + ex.getMessage(), ex);
        } catch (IOException e) {
            throw new RuntimeException(" File " + configurationPath + " not found. " + e.getMessage(), e);

        }
    }

    private void validateDomainObjectConfiguration(Validator validator) throws SAXException, IOException {
        validator.setErrorHandler(new ValidationErrorHandler());

        InputStream configurationInputStream = getFileInputStream(configurationPath);
        Source source = new StreamSource(configurationInputStream);
        validator.validate(source);
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
