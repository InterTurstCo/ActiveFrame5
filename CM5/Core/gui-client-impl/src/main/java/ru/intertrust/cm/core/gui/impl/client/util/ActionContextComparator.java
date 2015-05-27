package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.util.Comparator;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 27.05.2015
 */
public class ActionContextComparator implements Comparator<ActionContext> {
    @Override
    public int compare(ActionContext c1, ActionContext c2) {
        return c1.getActionConfig().getOrder()-c2.getActionConfig().getOrder();
    }
}
