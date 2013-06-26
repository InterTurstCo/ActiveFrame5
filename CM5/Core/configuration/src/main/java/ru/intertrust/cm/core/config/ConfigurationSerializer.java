package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.model.Configuration;

import java.io.StringWriter;
import java.util.Set;

/**
 * Предоставляет функциональность для сериализации/десериализации конфигурации
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:36 PM
 */
public class ConfigurationSerializer {

    private String configurationSchemaFilePath;
    private Set<String> configurationFilePaths;

    public ConfigurationSerializer() {
    }

    /**
     * Десериализует конфигурацию в строку
     * @param configuration конфигурация
     * @return сериализованная в строку конфигурация
     */
    public static String deserializeConfiguration(Configuration configuration) {
        Serializer serializer = new Persister();
        StringWriter stringWriter = new StringWriter();

        try {
            serializer.write(configuration, stringWriter);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to deserialize configuration");
        }

        return stringWriter.toString();
    }

    /**
     * Сериализует строку в конфигурацию без выполнения валидации на соответствие схеме конфигурации.
     * Данный метод предназначен для сериализации строк, представляющих собой ранее десериализованную конфигурацию
     * @param configurationString строка, содержащая конфигурацию
     * @return конфигурация
     * @throws ConfigurationException в случае ошибки сериализации
     */
    public static Configuration serializeTrustedConfiguration(String configurationString) throws
            ConfigurationException {
        try {
            Serializer serializer = new Persister();
            return serializer.read(Configuration.class, configurationString);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to serialize configuration from String", e);
        }
    }

    /**
     * Устанавливает путь к схеме конфигурации
     * @param configurationSchemaFilePath путь к схеме конфигурации
     */
    public void setConfigurationSchemaFilePath(String configurationSchemaFilePath) {
        this.configurationSchemaFilePath = configurationSchemaFilePath;
    }

    /**
     * Устаннавливает пити к конфигурационным файлам
     * @param configurationFilePaths пути к конфигурационным файлам
     */
    public void setConfigurationFilePaths(Set<String> configurationFilePaths) {
        this.configurationFilePaths = configurationFilePaths;
    }

    /**
     * Сериализует конфигурационные файлы в общий объект конфигруации, предварительно валидируя конфигурационные
     * файлы на соответствие схеме конфигурации
     * @return конфигурация, содержащая информацию всех конфигурационных файлов
     * @throws Exception
     */
    public Configuration serializeConfiguration() throws Exception {
        if(configurationFilePaths == null || configurationFilePaths.isEmpty()) {
            throw new RuntimeException("Configuration paths aren't specified");
        }

        Configuration combinedConfiguration = new Configuration();

        for(String configurationFilePath : configurationFilePaths) {
            Configuration partialConfiguration = serializeConfiguration(configurationFilePath);
            combineConfigurations(partialConfiguration, combinedConfiguration);
        }

        return combinedConfiguration;
    }

    private Configuration combineConfigurations(Configuration source, Configuration destination) {
        if(destination == null) {
            throw new IllegalArgumentException("Destination Configuration cannot be null");
        }

        if(source != null) {
            destination.getConfigurationList().addAll(source.getConfigurationList());
        }

        return destination;
    }

    /**
     * Сериализация конфигурации в Java класс.
     * @param configurationFilePath путь к файлу конфигурации
     * @return {@link ru.intertrust.cm.core.config.model.Configuration}
     * @throws Exception
     */
    private Configuration serializeConfiguration(String configurationFilePath) throws Exception {
        ConfigurationSchemaValidator schemaValidator = new ConfigurationSchemaValidator(configurationFilePath,
                configurationSchemaFilePath);
        schemaValidator.validate();

        Serializer serializer = new Persister();
        return serializer.read(Configuration.class, FileUtils.getFileInputStream(configurationFilePath));
    }
}
