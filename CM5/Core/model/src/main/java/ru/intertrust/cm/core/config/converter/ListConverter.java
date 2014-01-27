package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.List;

/**
 * Конвертер для сериализации конфигурации
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public abstract class ListConverter<T>  implements org.simpleframework.xml.convert.Converter<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(InputNode inputNode) throws Exception {
        T configuration = create();

        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        InputNode nexNode;

        while ((nexNode = inputNode.getNext()) != null) {
            Class nodeClass = configurationClassesCache.getClassByTagName(nexNode.getName());
            if (nodeClass == null) {
                continue;
            }

            getList(configuration).add(serializer.read(nodeClass, nexNode));
        }

        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputNode outputNode, T configuration) throws Exception {
        if (configuration == null) {
            return;
        }

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        for(Object configItem : getList(configuration)) {
            serializer.write(configItem, outputNode);
        }
    }

    public abstract T create();

    public abstract List getList(T configuration);

}
