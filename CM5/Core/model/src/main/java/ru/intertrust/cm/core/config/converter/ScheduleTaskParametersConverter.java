package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

public class ScheduleTaskParametersConverter implements Converter<ScheduleTaskParameters>  {

    @Override
    public ScheduleTaskParameters read(InputNode node) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();

        InputNode customParameters = node.getNext();

        Class<? extends ScheduleTaskParameters> parametersClass = configurationClassesCache.getClassByTagName(customParameters.getName());
        return (ScheduleTaskParameters) serializer.read(parametersClass, customParameters);
    }

    @Override
    public void write(OutputNode node, ScheduleTaskParameters parameters) throws Exception {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        serializer.write(parameters, node);
    }
}
