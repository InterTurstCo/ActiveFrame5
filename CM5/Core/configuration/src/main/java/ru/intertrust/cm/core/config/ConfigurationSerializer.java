package ru.intertrust.cm.core.config;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Предоставляет функциональность для сериализации/десериализации конфигурации
 * @author vmatsukevich Date: 6/12/13 Time: 5:36 PM
 */
public class ConfigurationSerializer {
    @Autowired
    ModuleService moduleService;

    /**
     * Создает {@link ConfigurationSerializer}
     */
    public ConfigurationSerializer() {
    }

    /**
     * Сериализует конфигурацию в строку
     * @param configuration
     *            конфигурация
     * @return сериализованная в строку конфигурация
     */
    public static String serializeConfiguration(Configuration configuration) {
        StringWriter stringWriter = new StringWriter();

        try {
            createSerializerInstance().write(configuration, stringWriter);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to deserialize configuration");
        }

        return stringWriter.toString();
    }

    /**
     * Десериализует строку в конфигурацию без выполнения валидации на соответствие схеме конфигурации. Данный метод
     * предназначен для десериализации строк, представляющих собой ранее сериализованную конфигурацию
     * @param configurationString
     *            строка, содержащая конфигурацию
     * @return конфигурация
     * @throws ConfigurationException
     *             в случае ошибки десериализации
     */
    public Configuration deserializeTrustedConfiguration(String configurationString) throws
            ConfigurationException {
        try {
            return createSerializerInstance().read(Configuration.class, configurationString);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to serialize configuration from String", e);
        }
    }

    /**
     * Десериализует конфигурационные файлы в общий объект конфигруации, предварительно валидируя конфигурационные файлы
     * на соответствие схеме конфигурации
     * @return конфигурация, содержащая информацию всех конфигурационных файлов
     * @throws Exception
     */
    public Configuration deserializeConfiguration() throws Exception {

        Configuration combinedConfiguration = new Configuration();

        List<String> schemaPaths = new ArrayList<String>();

        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getConfigurationSchemaPath() != null
                    && !schemaPaths.contains(moduleConfiguration.getConfigurationSchemaPath())) {
                schemaPaths.add(moduleConfiguration.getConfigurationSchemaPath());
            }
            if (moduleConfiguration.getConfigurationPaths() != null) {
                for (String configurationFilePath : moduleConfiguration.getConfigurationPaths()) {
                    Configuration partialConfiguration = deserializeConfiguration(configurationFilePath, schemaPaths);
                    combineConfigurations(partialConfiguration, combinedConfiguration);
                }
            }
        }

        return combinedConfiguration;
    }

    private Configuration combineConfigurations(Configuration source, Configuration destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination Configuration cannot be null");
        }

        if (source != null) {
            destination.getConfigurationList().addAll(source.getConfigurationList());
        }

        return destination;
    }

    /**
     * Десериализация конфигурации в Java класс.
     * @param configurationFilePath
     *            путь к файлу конфигурации
     * @return {@link ru.intertrust.cm.core.config.base.Configuration}
     * @throws Exception
     */
    private Configuration deserializeConfiguration(String configurationFilePath,
            List<String> configurationSchemaFilePath)
            throws Exception {
        try {
            InputStream[] schemaInputStreams = new InputStream[configurationSchemaFilePath.size()];

            for (int i = 0; i < configurationSchemaFilePath.size(); i++) {
                schemaInputStreams[i] = FileUtils.getFileInputStream(configurationSchemaFilePath.get(i));
            }

            ConfigurationSchemaValidator schemaValidator =
                    new ConfigurationSchemaValidator(FileUtils.getFileInputStream(configurationFilePath),
                            schemaInputStreams);
            schemaValidator.validate();

            return createSerializerInstance().read(Configuration.class,
                    FileUtils.getFileInputStream(configurationFilePath));
        } catch (Exception ex) {
            throw new ConfigurationException("Error load " + configurationFilePath, ex);
        }
    }

    private static Serializer createSerializerInstance() {
        Strategy strategy = new AnnotationStrategy();
        return new Persister(strategy);
    }

    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

}
