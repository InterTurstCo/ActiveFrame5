package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.01.14
 *         Time: 11:40
 */
public abstract class LabelRenderer implements ComponentHandler {
    public abstract String composeString(FieldPath[] fieldPaths, WidgetContext context);
}
