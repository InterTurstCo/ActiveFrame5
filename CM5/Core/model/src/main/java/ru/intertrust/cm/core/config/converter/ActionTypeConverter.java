package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import ru.intertrust.cm.core.config.gui.action.ActionType;

/**
 * @author Sergey.Okolot
 *         Created on 24.04.2014 12:22.
 */
public class ActionTypeConverter implements Converter<ActionType> {
    private static final String ACTION_TYPE_ATTR = "type";
    @Override
    public ActionType read(InputNode node) throws Exception {
        final String actionType = node.getAttribute(ACTION_TYPE_ATTR).getValue();
        return ActionType.valueOf(actionType);
    }

    @Override
    public void write(OutputNode node, ActionType value) throws Exception {
        node.setAttribute(ACTION_TYPE_ATTR, value.name());

    }
}
