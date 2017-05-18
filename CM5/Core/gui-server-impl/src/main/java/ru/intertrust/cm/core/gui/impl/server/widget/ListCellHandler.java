package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.ListCellConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListCellState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by Ravil on 18.05.2017.
 */
@ComponentName("list-cell")
public class ListCellHandler extends WidgetHandler implements SelfManagingWidgetHandler {
    private ListCellState widgetState;

    @Override
    public ListCellState getInitialState(WidgetContext context) {
        ListCellConfig config =  context.getWidgetConfig();
        widgetState = new ListCellState();
        widgetState.setHeaderValue(config.getHeaderValue());
        widgetState.setCounterRequired((config.getCounterRequired()!=null)?config.getCounterRequired():false);
        return widgetState;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
