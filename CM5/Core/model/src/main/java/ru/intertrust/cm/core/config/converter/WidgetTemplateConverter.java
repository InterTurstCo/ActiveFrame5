package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.template.WidgetTemplateConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.08.2015
 *         Time: 21:36
 */
public class WidgetTemplateConverter implements Converter<WidgetTemplateConfig> {

    @Override
    public WidgetTemplateConfig read(InputNode inputNode) throws Exception {
        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        WidgetTemplateConfig configuration = new WidgetTemplateConfig();
        String name = inputNode.getAttribute("name").getValue();
        configuration.setName(name);
        InputNode nexNode = inputNode.getNext();

        if(nexNode != null){
            Class nodeClass = configurationClassesCache.getClassByTagName(nexNode.getName());
            WidgetConfig widgetConfig = (WidgetConfig) serializer.read(nodeClass, nexNode);
            configuration.setWidgetConfig(widgetConfig);
        }

        return configuration;
    }

    @Override
    public void write(OutputNode outputNode, WidgetTemplateConfig configuration) throws Exception {
        if (configuration == null) {
            return;
        }

        outputNode.setAttribute("name", configuration.getName());
        
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        serializer.write(configuration.getWidgetConfig(), outputNode);
    }

}
