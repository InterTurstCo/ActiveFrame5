package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.gui.form.BodyConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfigMarker;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.08.2015
 *         Time: 18:38
 */
public class BodyConverter implements Converter<BodyConfig> {
    @Override
    public BodyConfig read(InputNode node) throws Exception {
        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        BodyConfig configuration = new BodyConfig();
        String displaySingleTabValue = node.getAttribute("display-single-tab").getValue();
        boolean displaySingleTab = Boolean.getBoolean(displaySingleTabValue);
        configuration.setDisplaySingleTab(displaySingleTab);
        InputNode nexNode;

        while ((nexNode = node.getNext()) != null) {
            Class nodeClass = configurationClassesCache.getClassByTagName(nexNode.getName());
            TabConfigMarker marker = (TabConfigMarker) serializer.read(nodeClass, nexNode);
            configuration.getTabConfigMarkers().add(marker);
            if (TabConfig.class == marker.getClass()) {
                TabConfig tabConfig = (TabConfig) marker;
                configuration.getTabs().add(tabConfig);
            }
        }


        return configuration;
    }

    @Override
    public void write(OutputNode outputNode, BodyConfig configuration) throws Exception {
        if (configuration == null) {
            return;
        }

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        outputNode.setAttribute("display-single-tab", Boolean.valueOf(configuration.isDisplaySingleTab()).toString());
        for (TabConfigMarker configItem : configuration.getTabConfigMarkers()) {
            serializer.write(configItem, outputNode);
        }
    }
}
