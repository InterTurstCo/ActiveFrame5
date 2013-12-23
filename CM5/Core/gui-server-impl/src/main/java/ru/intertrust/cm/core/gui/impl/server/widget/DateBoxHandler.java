package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("date-box")
public class DateBoxHandler extends SingleObjectWidgetHandler {
    @Override
    public DateBoxState getInitialState(WidgetContext context) {
        return new DateBoxState(context.<Date>getFieldPlainValue());
    }

    @Override
    public Value getValue(ValueEditingWidgetState state) {
        return new TimestampValue(((DateBoxState) state).getDate());
    }
}
