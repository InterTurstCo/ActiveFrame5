package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:00
 */
@ComponentName("integer-box")
public class IntegerBoxHandler extends WidgetHandler {
    @Override
    public IntegerBoxState getInitialState(WidgetContext context) {
        return new IntegerBoxState(context.<Long>getFieldPathPlainValue());
    }
}
