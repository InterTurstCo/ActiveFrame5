package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.converter.TopLevelConfigurationCache;

public class ActionSettingsConverter implements Converter<ActionSettings> {

    @Override
    public ActionSettings read(InputNode node) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        TopLevelConfigurationCache topLevelConfigurationCache = TopLevelConfigurationCache.getInstance();
        InputNode customSettings = node.getNext();
        Class actionSettingsClass = topLevelConfigurationCache.getClassByTagName(customSettings.getName());
        return (ActionSettings) serializer.read(actionSettingsClass, customSettings);
    }

    @Override
    public void write(OutputNode node, ActionSettings actionSettings) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        serializer.write(actionSettings, node);
    }

}
