package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import ru.intertrust.cm.core.config.model.base.Configuration;
import ru.intertrust.cm.core.model.FatalException;

import java.io.InputStream;
import java.io.StringWriter;
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

    /**
     * Создает {@link ConfigurationSerializer}
     */
    public ConfigurationSerializer() {
    }

    /**
     * Сериализует конфигурацию в строку
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
     * Устанавливает путь к схеме конфигурации ядра
     * @param coreConfigurationSchemaFilePath путь к схеме конфигурации
     */
    public void setCoreConfigurationSchemaFilePath(String coreConfigurationSchemaFilePath) {
        this.coreConfigurationSchemaFilePath = coreConfigurationSchemaFilePath;
    }

    /**
     * Устаннавливает пути к конфигурационным файлам ядра
     * @param coreConfigurationFilePaths пути к конфигурационным файлам
     */
    public void setCoreConfigurationFilePaths(Set<String> coreConfigurationFilePaths) {
        this.coreConfigurationFilePaths = coreConfigurationFilePaths;
    }

    /**
     * Устанавливает путь к конфигурации конфигураций модулей
     * @param modulesConfigurationPath путь к конфигурации конфигураций модулей
     */
    public void setModulesConfigurationPath(String modulesConfigurationPath) {
        this.modulesConfigurationPath = modulesConfigurationPath;
    }

    /**
     * Устанавливает путь к схеме конфигруции конфигураций модулей
     * @param modulesConfigurationSchemaPath уть к схеме конфигруции конфигураций модулей
     */
    public void setModulesConfigurationSchemaPath(String modulesConfigurationSchemaPath) {
        this.modulesConfigurationSchemaPath = modulesConfigurationSchemaPath;
    }

    /**
     * Устанавливает путь к папке конфигураций модулей
     * @param modulesConfigurationFolder
     */
    public void setModulesConfigurationFolder(String modulesConfigurationFolder) {
        this.modulesConfigurationFolder = modulesConfigurationFolder;
    }

    /**
     * Десериализует строку в конфигурацию без выполнения валидации на соответствие схеме конфигурации.
     * Данный метод предназначен для десериализации строк, представляющих собой ранее сериализованную конфигурацию
     * @param configurationString строка, содержащая конфигурацию
     * @return конфигурация
     * @throws ConfigurationException в случае ошибки десериализации
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
     * Десериализует конфигурационные файлы в общий объект конфигруации, предварительно валидируя конфигурационные
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
     * Десериализация конфигурации в Java класс.
     * @param configurationFilePath путь к файлу конфигурации
     * @return {@link ru.intertrust.cm.core.config.model.base.Configuration}
     * @throws Exception
     */
   public Configuration deserializeConfiguration(String configurationFilePath) throws Exception {
       try{
        ConfigurationSchemaValidator schemaValidator = new ConfigurationSchemaValidator(configurationFilePath,
                coreConfigurationSchemaFilePath);
        schemaValidator.validate();

        return createSerializerInstance().read(Configuration.class, FileUtils.getFileInputStream(configurationFilePath));
       }catch(Exception ex){
           throw new ConfigurationException("Error load " + configurationFilePath, ex);
       }
    }

    private Configuration deserializeModuleConfiguration(ModuleConfig moduleConfig) throws Exception {
        String moduleConfigurationFullPath = modulesConfigurationFolder + moduleConfig.getPath();
        InputStream configInputStream = FileUtils.getFileInputStream(moduleConfigurationFullPath);

        ConfigurationSchemaValidator schemaValidator =
                new ConfigurationSchemaValidator(configInputStream, getModuleSchemaInputStreams(moduleConfig));

        schemaValidator.validate();

        configInputStream = FileUtils.getFileInputStream(moduleConfigurationFullPath);
        return createSerializerInstance().read(Configuration.class, configInputStream);
    }

    private InputStream[] getModuleSchemaInputStreams(ModuleConfig moduleConfig) {
        InputStream coreSchemaInputStream = FileUtils.getFileInputStream(coreConfigurationSchemaFilePath);

        InputStream[] schemaInputStreams;
        if (moduleConfig.getSchemaPath() != null) {
            String schemaFullPath = modulesConfigurationFolder + moduleConfig.getSchemaPath();
            InputStream schemaInputStream = FileUtils.getFileInputStream(schemaFullPath);
            schemaInputStreams = new InputStream[] {coreSchemaInputStream, schemaInputStream};
        } else {
            schemaInputStreams = new InputStream[] {coreSchemaInputStream};
        }

        return schemaInputStreams;
    }

    private static Serializer createSerializerInstance() {
        Strategy strategy = new AnnotationStrategy();
        return new Persister(strategy);
    }
}
