package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import ru.intertrust.cm.core.config.gui.action.ActionDisplayType;

/**
 * @author Sergey.Okolot
 *         Created on 24.04.2014 12:23.
 */
public class ActionDisplayTypeConverter implements Converter<ActionDisplayType> {
    private static final String DISPLAY_TYPE_ATTR = "display";
    @Override
    public ActionDisplayType read(InputNode node) throws Exception {
        final String dispalyType = node.getAttribute(DISPLAY_TYPE_ATTR).getValue();
        return ActionDisplayType.valueOf(dispalyType);
    }

    @Override
    public void write(OutputNode node, ActionDisplayType value) throws Exception {
        node.setAttribute(DISPLAY_TYPE_ATTR, value.name());
    }
}
