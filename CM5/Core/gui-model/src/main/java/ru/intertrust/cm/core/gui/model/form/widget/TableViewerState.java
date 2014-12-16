package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 21:00
 */
public class TableViewerState extends WidgetState {
    private TableViewerConfig tableViewerConfig;

    public TableViewerState() {
    }

    public TableViewerState(TableViewerConfig tableViewerConfig) {
        this.tableViewerConfig = tableViewerConfig;
    }

    public TableViewerConfig getTableViewerConfig() {
        return tableViewerConfig;
    }

}
