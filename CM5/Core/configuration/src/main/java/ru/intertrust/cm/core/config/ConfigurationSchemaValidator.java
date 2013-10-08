package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ru.intertrust.cm.core.model.FatalException;

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
 * Валидирует файл конфигурации на соответствие схеме конфигурации
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 6:13 PM
 */
public class ConfigurationSchemaValidator {

    final static Logger logger = LoggerFactory.getLogger(ConfigurationSchemaValidator.class);

    private String configurationPath;
    private String configurationSchemaPath;

    private InputStream configurationInputStream;
    private InputStream[] configurationSchemaInputStreams;

    public ConfigurationSchemaValidator(String configurationPath, String configurationSchemaPath) {
        this.configurationPath = configurationPath;
        this.configurationSchemaPath = configurationSchemaPath;
    }

    public ConfigurationSchemaValidator(InputStream configurationInputStream, String configurationSchemaPath) {
        this.configurationInputStream = configurationInputStream;
        this.configurationSchemaPath = configurationSchemaPath;
    }

    public ConfigurationSchemaValidator(InputStream configurationInputStream,
                                        InputStream... configurationSchemaInputStreams) {
        this.configurationInputStream = configurationInputStream;
        this.configurationSchemaInputStreams = configurationSchemaInputStreams;
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

    public InputStream getConfigurationInputStream() {
        return configurationInputStream;
    }

    public void setConfigurationInputStream(InputStream configurationInputStream) {
        this.configurationInputStream = configurationInputStream;
    }

    public InputStream[] getConfigurationSchemaInputStreams() {
        return configurationSchemaInputStreams;
    }

    public void setConfigurationSchemaInputStreams(InputStream[] configurationSchemaInputStream) {
        this.configurationSchemaInputStreams = configurationSchemaInputStream;
    }

    /**
     * Выполняет валидацию конфигурации на предмет соответствия XSD схеме и логическую валидацию.
     */
    public void validate() {
        if (configurationPath == null && configurationInputStream == null) {
            throw new FatalException("Please set the configurationPath for ConfigurationSchemaValidator before validating");
        }

        if (configurationSchemaPath == null && configurationSchemaInputStreams == null) {
            throw new FatalException("Please set the configurationSchemaPath for ConfigurationLogicalValidator before " +
                    "validating");
        }

        validateAgainstXSD();
    }

    private void validateAgainstXSD() {
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


        try {
            if (configurationSchemaInputStreams == null) {
                configurationSchemaInputStreams = new InputStream[] {getFileInputStream(configurationSchemaPath)};
            }

            Source[] schemaSources = new StreamSource[configurationSchemaInputStreams.length];
            for (int i = 0; i < configurationSchemaInputStreams.length; i ++) {
                schemaSources[i] = new StreamSource(configurationSchemaInputStreams[i]);
            }

            Schema schema = factory.newSchema(schemaSources);
            Validator validator = schema.newValidator();

            validateDomainObjectConfiguration(validator);
            logger.info("Document is valid against XSD");
        } catch (SAXException ex) {
            throw new FatalException("Document " + configurationPath + " is not valid against XSD schema: " + ex
                    .getMessage(), ex);
        } catch (IOException e) {
            throw new FatalException(" File " + configurationPath + " not found. " + e.getMessage(), e);

        }
    }

    private void validateDomainObjectConfiguration(Validator validator) throws SAXException, IOException {
        validator.setErrorHandler(new ValidationErrorHandler());

        if (configurationInputStream == null) {
            configurationInputStream = FileUtils.getFileInputStream(configurationPath);
        }

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
            logger.warn("Configuration Schema Validation Error", ex);
        }

        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }
    }
}
