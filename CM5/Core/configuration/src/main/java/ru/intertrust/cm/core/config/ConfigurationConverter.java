package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.TopLevelConfig;

/**
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public class ConfigurationConverter implements org.simpleframework.xml.convert.Converter<Configuration> {

    final static Logger logger = LoggerFactory.getLogger(ConfigurationConverter.class);

    @Override
    public Configuration read(InputNode inputNode) throws Exception {
        Configuration configuration = new Configuration();

        TopLevelConfigurationCache topLevelConfigurationCache = TopLevelConfigurationCache.getInstance();
        Serializer serializer = new Persister();
        InputNode nexNode;

        while ((nexNode = inputNode.getNext()) != null) {
            Class nodeClass = topLevelConfigurationCache.getClassByTagName(nexNode.getName());
            if (nodeClass == null) {
                continue;
            }

            configuration.getConfigurationList().add((TopLevelConfig) serializer.read(nodeClass, nexNode));
        }

        return configuration;
    }

    @Override
    public void write(OutputNode outputNode, Configuration configuration) throws Exception {
        if (configuration == null) {
            return;
        }

        Serializer serializer = new Persister();

        for(Object configItem : configuration.getConfigurationList()) {
            serializer.write(configItem, outputNode);
        }
    }
}
