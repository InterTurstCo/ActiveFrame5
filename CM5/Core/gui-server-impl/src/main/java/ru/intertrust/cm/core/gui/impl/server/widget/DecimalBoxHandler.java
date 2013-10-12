package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 11:40
 */
@ComponentName("decimal-box")
public class DecimalBoxHandler extends WidgetHandler {
    @Override
    public DecimalBoxState getInitialState(WidgetContext context) {
        return new DecimalBoxState(context.<BigDecimal>getFieldPathPlainValue());
    }
}