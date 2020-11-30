package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.LoadedConfiguration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.ConfigurationDeserializationException;
import ru.intertrust.cm.core.config.converter.EnumTransform;
import ru.intertrust.cm.core.config.migration.MigrationScriptConfig;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Предоставляет функциональность для сериализации/десериализации конфигурации
 * @author vmatsukevich Date: 6/12/13 Time: 5:36 PM
 */
public class ConfigurationSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSerializer.class);

    @Autowired
    ModuleService moduleService;

    @Value("${configuration.deserializer.threads.count:10}")
    private int deserializationThreadsCount = 1;

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
            throw new ConfigurationException("Failed to serialize configuration");
        }

        return stringWriter.toString();
    }

    public static String serializeConfiguration(Object config) {
        StringWriter stringWriter = new StringWriter();

        try {
            createSerializerInstance().write(config, stringWriter);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to serialize configuration");
        }

        return stringWriter.toString();
    }

    /**
     * Десериализует набор конфигурационных файлов в Java-класс
     * @param files набор файлов конфигураций
     * @return конфигурация
     * @throws ConfigurationException
     *             в случае ошибки десериализации
     */
    public Configuration deserializeConfiguration(Collection<File> files) {
        try {
            Configuration configuration = new Configuration();
            for (File file : files) {
                new ConfigurationSchemaValidator(FileUtils.fileInputStream(file), getAllSchemaStreams()).validate();
                combineConfigurations(createSerializerInstance().read(Configuration.class, file), configuration);
            }
            return configuration;
        } catch (Throwable e) {
            throw new ConfigurationException("Failed to deserialize configuration", e);
        }
    }

    /**
     * Десериализует конфигурации в Java-класс
     * @param configurationString XML-конфигурация
     * @return конфигурация
     * @throws ConfigurationException
     *             в случае ошибки десериализации
     */
    public Configuration deserializeConfiguration(String configurationString) {
        try {
            ConfigurationSchemaValidator schemaValidator =
                    new ConfigurationSchemaValidator(
                            new ByteArrayInputStream(configurationString.getBytes(Charset.forName("UTF-8"))),
                            getAllSchemaStreams());
            schemaValidator.validate();
            return createSerializerInstance().read(Configuration.class, configurationString);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to deserialize configuration", e);
        }
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
    public Configuration deserializeLoadedConfiguration(String configurationString) throws
            ConfigurationException {
        try {
            return createSerializerInstance().read(LoadedConfiguration.class, configurationString);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to serialize configuration from string.\n" + e);
        }
    }

    /**
     * Десериализует конфигурационные файлы в общий объект конфигруации, предварительно валидируя конфигурационные файлы
     * на соответствие схеме конфигурации
     * @return конфигурация, содержащая информацию всех конфигурационных файлов
     * @throws Exception
     */
    public Configuration deserializeConfiguration() throws Exception {
        logger.info("Start deserialize configuration");
        List<String> schemaPaths = new ArrayList<String>();

        ExecutorService executor = Executors.newFixedThreadPool(deserializationThreadsCount);

        Map<String, List<Configuration>> moduleConfigsByName = new LinkedHashMap<>(100); // linked map just for tests to run
        final List<Future<DeserializationResult>> futures = new ArrayList<>();
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            final String moduleName = moduleConfiguration.getName();
            if (moduleConfigsByName.containsKey(moduleName)) {
                throw new ConfigurationException("Module with name: " + moduleName + " is defined more than 1 time");
            }
            if (moduleConfiguration.getConfigurationSchemaPath() != null
                    && !schemaPaths.contains(moduleConfiguration.getConfigurationSchemaPath())) {
                schemaPaths.add(moduleConfiguration.getConfigurationSchemaPath());
            }
            final List<String> configurationPaths = moduleConfiguration.getConfigurationPaths();
            final ArrayList<Configuration> modulePartialConfigurations = new ArrayList<>(configurationPaths == null ? 0 : configurationPaths.size());
            moduleConfigsByName.put(moduleName, modulePartialConfigurations);
            if (configurationPaths != null) {
                List<String> schemaPathsClone = new ArrayList<>(schemaPaths);
                for (String configurationFilePath : configurationPaths) {
                    // CMFIVE-37608 Десериализуем в параллельных потоках
                    Future<DeserializationResult> future = executor.submit(() -> {
                        return tryDeserializeConfiguration(configurationFilePath, schemaPathsClone, moduleConfiguration);
                    });
                    futures.add(future);
                }
            }
        }

        // Ждем окончания всех потоков и собираем результат
        final List<Exception> exceptionList = new ArrayList<>();
        for (Future<DeserializationResult> future : futures) {
            DeserializationResult result = future.get();
            if (result.error != null) {
                exceptionList.add(result.error);
            } else {
                moduleConfigsByName.get(result.moduleName).add(result.partialConfiguration);
            }
        }

        // Проверка на ошибки
        if (!exceptionList.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Failed to deserialize configuration.\n");
            for (Exception exception : exceptionList) {
                errorMessage.append(exception.getMessage());
            }

            throw new ConfigurationException(errorMessage.toString());
        }

        // Формирование общей конфигурации
        Configuration combinedConfiguration = new Configuration();
        for (Map.Entry<String, List<Configuration>> moduleConfigs : moduleConfigsByName.entrySet()) {
            final List<Configuration> partialConfigurations = moduleConfigs.getValue();
            for (Configuration partialConfiguration : partialConfigurations) {
                combineConfigurations(partialConfiguration, combinedConfiguration);
            }
        }
        logger.info("End deserialize configuration");
        return combinedConfiguration;
    }

    private DeserializationResult tryDeserializeConfiguration(String configurationFilePath,
                                                              List<String> configurationSchemaFilePath,
                                                              ModuleConfiguration moduleConfiguration){
        DeserializationResult result = new DeserializationResult();
        result.moduleName = moduleConfiguration.getName();
        try {
            Configuration partialConfiguration = deserializeConfiguration(configurationFilePath, configurationSchemaFilePath, moduleConfiguration);
            result.partialConfiguration = partialConfiguration;
        } catch (Exception ex) {
            result.error = ex;
            logger.error("Error deserialization config {}", configurationFilePath, ex);
        }
        return result;

    }

    private InputStream[] getAllSchemaStreams() {
        HashSet<String> schemaPaths = new HashSet<>();

        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getConfigurationSchemaPath() != null
                    && !schemaPaths.contains(moduleConfiguration.getConfigurationSchemaPath())) {
                schemaPaths.add(moduleConfiguration.getConfigurationSchemaPath());
            }
        }
        InputStream[] result = new InputStream[schemaPaths.size()];
        int i = 0;
        for (String schemaPath : schemaPaths) {
            result[i++] = FileUtils.getFileInputStream(schemaPath);
        }
        return result;
    }

    private Configuration combineConfigurations(Configuration source, Configuration destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination Configuration cannot be null");
        }

        if (source != null) {
            final List<TopLevelConfig> destinationTopLevelConfigs = destination.getConfigurationList();
            final List<TopLevelConfig> sourceTopLevelConfigs = source.getConfigurationList();
            for (TopLevelConfig sourceTopLevelConfig : sourceTopLevelConfigs) {
                final boolean isMigrationScript = sourceTopLevelConfig instanceof MigrationScriptConfig;
                if (isMigrationScript) {
                    ((MigrationScriptConfig) sourceTopLevelConfig).setModuleName(source.getModuleName());
                }
                destinationTopLevelConfigs.add(sourceTopLevelConfig);
            }
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
                                                   List<String> configurationSchemaFilePath, ModuleConfiguration moduleConfiguration)
            throws Exception {
        final URL moduleUrl = moduleConfiguration.getModuleUrl();
        try {
            InputStream[] schemaInputStreams = new InputStream[configurationSchemaFilePath.size()];

            for (int i = 0; i < configurationSchemaFilePath.size(); i++) {
                schemaInputStreams[i] = FileUtils.getFileInputStream(configurationSchemaFilePath.get(i));
            }

            ConfigurationSchemaValidator schemaValidator =
                    new ConfigurationSchemaValidator(getStreamFromUrl(moduleUrl, configurationFilePath),
                            schemaInputStreams);
            schemaValidator.validate();
            final InputStream is = getStreamFromUrl(moduleUrl, configurationFilePath);
            final Configuration result = createSerializerInstance().read(Configuration.class, is);
            result.setModuleName(moduleConfiguration.getName());
            return result;
        } catch (ConfigurationDeserializationException e) {
            e.setConfigurationFilePath(configurationFilePath);
            throw e;
        } catch (Exception ex) {
            logger.error("Error deserializeConfiguration", ex);
            throw new ConfigurationException("Error loading " + configurationFilePath + ":" + ex.getClass() + " " + ex.getMessage());
        }
    }

    private static Serializer createSerializerInstance() {
        Strategy strategy = new AnnotationStrategy();
        Matcher matcher = new Matcher() {
            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public Transform match(Class type) throws Exception {
                if (type.isEnum()) {
                    return new EnumTransform((Class<? extends Enum>) type);
                }
                return null;
            }
        };
        return new Persister(strategy, matcher);
    }

    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    private InputStream getStreamFromUrl(URL baseUrl, String path) throws IOException{
        URL resultUrl = new URL(baseUrl.toString() + path);
        return resultUrl.openStream();
    }

    private static class DeserializationResult{
        private Configuration partialConfiguration;
        private Exception error;
        private String moduleName;
    }
}
