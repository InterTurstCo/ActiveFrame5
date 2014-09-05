package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.ErrorIgnoringConfiguration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.List;

/**
 * Конвертер для сериализации конфигурации
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public class ErrorIgnoringConfigurationConverter extends ListConverter<ErrorIgnoringConfiguration> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ErrorIgnoringConfigurationConverter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorIgnoringConfiguration read(InputNode inputNode) throws Exception {
        ErrorIgnoringConfiguration configuration = create();

        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        InputNode nexNode;

        while ((nexNode = inputNode.getNext()) != null) {
            Class nodeClass = configurationClassesCache.getClassByTagName(nexNode.getName());
            if (nodeClass == null) {
                continue;
            }

            TopLevelConfig deserializedObject = null;
            try {
                deserializedObject = (TopLevelConfig) serializer.read(nodeClass, nexNode);
            } catch(Exception e) {
                if (DomainObjectTypeConfig.class.equals(nodeClass)) {
                    throw e;
                } else {
                    InputNode nameNode = nexNode.getAttribute("name");
                    String errorMessage = "Failed to serialize configuration item: type='" + nexNode.getName() + "'" +
                            (nameNode != null && nameNode.getValue() != null ? ", name='" + nameNode.getValue() + "'" : "");
                    logger.error(errorMessage);
                }
            }

            if (deserializedObject != null) {
                getList(configuration).add(deserializedObject);
            }
        }

        return configuration;
    }

    @Override
    public ErrorIgnoringConfiguration create() {
        return new ErrorIgnoringConfiguration();
    }

    @Override
    public List<TopLevelConfig> getList(ErrorIgnoringConfiguration configuration) {
        return configuration.getConfigurationList();
    }
}
