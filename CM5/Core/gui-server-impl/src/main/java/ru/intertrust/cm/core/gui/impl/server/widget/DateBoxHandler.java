package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("date-box")
public class DateBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public DateBoxState getInitialState(WidgetContext context) {
        //DateBoxConfig config = context.getWidgetConfig();
        //RangeDateConfig rangeDateConfig = config.getRangeDateValueConfig();
        DateBoxState dateBoxState = new DateBoxState();
        dateBoxState.setDate(context.<Date>getFieldPlainValue());
        //dateBoxState.setRangeDateValueConfig(rangeDateConfig);
        return dateBoxState;
    }

    @Override
    public Value getValue(WidgetState state) {
        return new DateTimeValue(((DateBoxState) state).getDate());
    }
}
