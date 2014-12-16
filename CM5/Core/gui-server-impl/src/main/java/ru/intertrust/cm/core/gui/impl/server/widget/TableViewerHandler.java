package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:29
 */
@ComponentName("table-viewer")
public class TableViewerHandler extends WidgetHandler implements SelfManagingWidgetHandler {
    @Override
    public TableViewerState getInitialState(WidgetContext context) {

        return new TableViewerState((TableViewerConfig) context.getWidgetConfig());
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
