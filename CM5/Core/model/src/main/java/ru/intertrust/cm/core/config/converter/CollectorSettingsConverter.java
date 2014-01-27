package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.CollectorSettings;

public class CollectorSettingsConverter implements Converter<CollectorSettings> {

    @Override
    public CollectorSettings read(InputNode node) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();

        InputNode customSettings = node.getNext();

        Class collectorSettingsClass = configurationClassesCache.getClassByTagName(customSettings.getName());
        return (CollectorSettings) serializer.read(collectorSettingsClass, customSettings);
    }

    @Override
    public void write(OutputNode node, CollectorSettings collectorSettings) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        serializer.write(collectorSettings, node);
    }

}
