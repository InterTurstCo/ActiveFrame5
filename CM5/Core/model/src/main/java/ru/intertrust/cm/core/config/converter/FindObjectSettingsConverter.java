package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import ru.intertrust.cm.core.config.FindObjectSettings;

public class FindObjectSettingsConverter implements Converter<FindObjectSettings> {

    @Override
    public FindObjectSettings read(InputNode node) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();

        InputNode customSettings = node.getNext();

        Class findObjectSettingsClass = configurationClassesCache.getClassByTagName(customSettings.getName());
        return (FindObjectSettings) serializer.read(findObjectSettingsClass, customSettings);
    }

    @Override
    public void write(OutputNode node, FindObjectSettings findObjectSettings) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        serializer.write(findObjectSettings, node);
    }

}
