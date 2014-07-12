package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.OnLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.OnUnlinkConfig;

/**
* @author Denis Mitavskiy
*         Date: 07.05.14
*         Time: 13:39
*/
public final class FieldPathOnDeleteActionConverter implements org.simpleframework.xml.convert.Converter<FieldPathConfig> {
    @Override
    public FieldPathConfig read(InputNode node) throws Exception {
        final InputNode valueAttribute = node.getAttribute("value");
        final InputNode onDelete = node.getAttribute("on-root-delete");
        final InputNode domainObjectLinkerAttribute = node.getAttribute("domain-object-linker");

        final String value = valueAttribute == null ? null : valueAttribute.getValue();
        final String domainObjectLinker = domainObjectLinkerAttribute == null ? null : domainObjectLinkerAttribute.getValue();

        FieldPathConfig.OnDeleteAction onDeleteAction = onDelete == null ? null : FieldPathConfig.OnDeleteAction.getEnum(onDelete.getValue());
        FieldPathConfig result = new FieldPathConfig();
        result.setValue(value);
        result.setOnRootDelete(onDeleteAction);
        result.setDomainObjectLinker(domainObjectLinker);

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        InputNode nexNode;

        while ((nexNode = node.getNext()) != null) {
            if ("on-link".equals(nexNode.getName())) {
                OnLinkConfig onLinkConfig = serializer.read(OnLinkConfig.class, nexNode);
                result.setOnLinkConfig(onLinkConfig);
            } else if ("on-unlink".equals(nexNode.getName())) {
                OnUnlinkConfig onUnlinkConfig = serializer.read(OnUnlinkConfig.class, nexNode);
                result.setOnUnlinkConfig(onUnlinkConfig);
            }

        }

        return result;
    }

    @Override
    public void write(OutputNode node, FieldPathConfig fieldPathConfig) throws Exception {
        final String value = fieldPathConfig.getValue();
        if (value != null && !value.isEmpty()) {
            node.setAttribute("value", value);
        }

        final FieldPathConfig.OnDeleteAction onDelete = fieldPathConfig.getOnRootDelete();
        if (onDelete != null) {
            node.setAttribute("on-root-delete", onDelete.getString());
        }

        final String domainObjectLinker = fieldPathConfig.getDomainObjectLinker();
        if (domainObjectLinker != null && !domainObjectLinker.isEmpty()) {
            node.setAttribute("domain-object-linker", domainObjectLinker);
        }

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        if (fieldPathConfig.getOnLinkConfig() != null) {
            serializer.write(fieldPathConfig.getOnLinkConfig(), node);
        }

        if (fieldPathConfig.getOnUnlinkConfig() != null) {
            serializer.write(fieldPathConfig.getOnUnlinkConfig(), node);
        }
    }
}
