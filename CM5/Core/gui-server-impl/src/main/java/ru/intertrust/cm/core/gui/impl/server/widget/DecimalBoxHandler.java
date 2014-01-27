package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 11:40
 */
@ComponentName("decimal-box")
public class DecimalBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public DecimalBoxState getInitialState(WidgetContext context) {
        return new DecimalBoxState(context.<BigDecimal>getFieldPlainValue());
    }

    @Override
    public Value getValue(WidgetState state) {
        return new DecimalValue(((DecimalBoxState) state).getNumber());
    }
}