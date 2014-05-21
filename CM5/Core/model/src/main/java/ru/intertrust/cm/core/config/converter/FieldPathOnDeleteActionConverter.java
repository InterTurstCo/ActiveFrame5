package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;

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
        final String value = valueAttribute == null ? null : valueAttribute.getValue();
        FieldPathConfig.OnDeleteAction onDeleteAction = onDelete == null ? null : FieldPathConfig.OnDeleteAction.getEnum(onDelete.getValue());
        FieldPathConfig result = new FieldPathConfig();
        result.setValue(value);
        result.setOnRootDelete(onDeleteAction);
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
    }
}
