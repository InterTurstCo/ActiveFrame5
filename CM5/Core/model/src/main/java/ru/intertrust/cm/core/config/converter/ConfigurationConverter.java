package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Конвертер для сериализации конфигурации
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public class ConfigurationConverter extends ListConverter<Configuration> {

    private final List<String> creationErrorList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration read(InputNode inputNode) throws Exception {
        Configuration configuration = create();

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
                InputNode nameNode = nexNode.getAttribute("name");
                String errorMessage = "Failed to serialize configuration item: type='" + nexNode.getName() + "'" +
                        (nameNode != null && nameNode.getValue() != null ? ", name='" + nameNode.getValue() + "'" : "");
                creationErrorList.add(errorMessage);
            }

            if (deserializedObject != null) {
                getList(configuration).add(deserializedObject);
            }
        }

        if (!creationErrorList.isEmpty()) {
            throw new ConfigurationDeserializationException(creationErrorList);
        }

        return configuration;
    }

    @Override
    public Configuration create() {
        return new Configuration();
    }

    @Override
    public List<TopLevelConfig> getList(Configuration configuration) {
        return configuration.getConfigurationList();
    }

}
