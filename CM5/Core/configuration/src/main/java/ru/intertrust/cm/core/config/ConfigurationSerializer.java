package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.model.FatalException;

import java.io.*;
import java.util.Set;

/**
 * Предоставляет функциональность для сериализации/десериализации конфигурации
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:36 PM
 */
public class ConfigurationSerializer {

    private String coreConfigurationSchemaFilePath;
    private Set<String> coreConfigurationFilePaths;

    private String modulesConfigurationFolder;
    private String modulesConfigurationPath;
    private String modulesConfigurationSchemaPath;

    private ModulesConfiguration modulesConfiguration;

    public ConfigurationSerializer() {
    }

    /**
     * Десериализует конфигурацию в строку
     * @param configuration конфигурация
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
     * Устанавливает путь к схеме конфигурации
     * @param coreConfigurationSchemaFilePath путь к схеме конфигурации
     */
    public void setCoreConfigurationSchemaFilePath(String coreConfigurationSchemaFilePath) {
        this.coreConfigurationSchemaFilePath = coreConfigurationSchemaFilePath;
    }

    /**
     * Устаннавливает пити к конфигурационным файлам
     * @param coreConfigurationFilePaths пути к конфигурационным файлам
     */
    public void setCoreConfigurationFilePaths(Set<String> coreConfigurationFilePaths) {
        this.coreConfigurationFilePaths = coreConfigurationFilePaths;
    }

    public void setModulesConfigurationPath(String modulesConfigurationPath) {
        this.modulesConfigurationPath = modulesConfigurationPath;
    }

    public void setModulesConfigurationSchemaPath(String modulesConfigurationSchemaPath) {
        this.modulesConfigurationSchemaPath = modulesConfigurationSchemaPath;
    }

    public String getModulesConfigurationFolder() {
        return modulesConfigurationFolder;
    }

    public void setModulesConfigurationFolder(String modulesConfigurationFolder) {
        this.modulesConfigurationFolder = modulesConfigurationFolder;
    }

    /**
     * Сериализует строку в конфигурацию без выполнения валидации на соответствие схеме конфигурации.
     * Данный метод предназначен для сериализации строк, представляющих собой ранее десериализованную конфигурацию
     * @param configurationString строка, содержащая конфигурацию
     * @return конфигурация
     * @throws ConfigurationException в случае ошибки сериализации
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
     * Сериализует конфигурационные файлы в общий объект конфигруации, предварительно валидируя конфигурационные
     * файлы на соответствие схеме конфигурации
     * @return конфигурация, содержащая информацию всех конфигурационных файлов
     * @throws Exception
     */
    public Configuration deserializeConfiguration() throws Exception {
        if (coreConfigurationFilePaths == null || coreConfigurationFilePaths.isEmpty()) {
            throw new FatalException("Core configuration paths aren't specified");
        }

        if (coreConfigurationSchemaFilePath == null || coreConfigurationSchemaFilePath.isEmpty()) {
            throw new FatalException("Core configuration schema paths aren't specified");
        }

        if (modulesConfiguration == null) {
            modulesConfiguration = deserializeModulesConfiguration();
        }

        Configuration combinedConfiguration = new Configuration();

        for(String configurationFilePath : coreConfigurationFilePaths) {
            Configuration partialConfiguration = deserializeConfiguration(configurationFilePath);
            combineConfigurations(partialConfiguration, combinedConfiguration);
        }

        for (ModuleConfig moduleConfig : modulesConfiguration.getModuleConfigs()) {
            Configuration partialConfiguration = deserializeModuleConfiguration(moduleConfig);
            combineConfigurations(partialConfiguration, combinedConfiguration);
        }

        return combinedConfiguration;
    }

    private ModulesConfiguration deserializeModulesConfiguration() throws Exception {
        if(modulesConfigurationPath == null || modulesConfigurationPath.isEmpty()) {
            throw new FatalException("ModulesConfiguration path isn't specified");
        }

        if(modulesConfigurationSchemaPath == null || modulesConfigurationSchemaPath.isEmpty()) {
            throw new FatalException("ModulesConfiguration schema path isn't specified");
        }

        if (modulesConfigurationFolder == null || modulesConfigurationFolder.isEmpty()) {
            throw new FatalException("ModulesConfigurationFolder isn't specified");
        }

        String modulesConfigurationFullPath = modulesConfigurationFolder + modulesConfigurationPath;

        InputStream moduleConfigurationInputStream = FileUtils.getFileInputStream(modulesConfigurationFullPath);
        ConfigurationSchemaValidator schemaValidator =
                new ConfigurationSchemaValidator(moduleConfigurationInputStream, modulesConfigurationSchemaPath);
        schemaValidator.validate();

        Serializer serializer = new Persister();
        return serializer.read(ModulesConfiguration.class, FileUtils.getFileInputStream(modulesConfigurationFullPath));
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
    private Configuration deserializeConfiguration(String configurationFilePath) throws Exception {
        ConfigurationSchemaValidator schemaValidator = new ConfigurationSchemaValidator(configurationFilePath,
                coreConfigurationSchemaFilePath);
        schemaValidator.validate();

        return createSerializerInstance().read(Configuration.class, FileUtils.getFileInputStream(configurationFilePath));
    }

    private Configuration deserializeModuleConfiguration(ModuleConfig moduleConfig) throws Exception {
        String moduleConfigurationFullPath = modulesConfigurationFolder + moduleConfig.getPath();
        String schemaFullPath = modulesConfigurationFolder + moduleConfig.getSchemaPath();

        InputStream configInputStream = FileUtils.getFileInputStream(moduleConfigurationFullPath);
        InputStream schemaInputStream = FileUtils.getFileInputStream(schemaFullPath);
        InputStream coreSchemaInputStream = FileUtils.getFileInputStream(coreConfigurationSchemaFilePath);

        InputStream[] schemaInputStreams = new InputStream[] {coreSchemaInputStream, schemaInputStream};

        ConfigurationSchemaValidator schemaValidator =
                new ConfigurationSchemaValidator(configInputStream, schemaInputStreams);

        //TODO: починить валидацию для конфигураций модулей
        //schemaValidator.validate();

        return createSerializerInstance().read(Configuration.class, configInputStream);
    }

    private static Serializer createSerializerInstance() {
        Strategy strategy = new AnnotationStrategy();
        return new Persister(strategy);
    }
}
