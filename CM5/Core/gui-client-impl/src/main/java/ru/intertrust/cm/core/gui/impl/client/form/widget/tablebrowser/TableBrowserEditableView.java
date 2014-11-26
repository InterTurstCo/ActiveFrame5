package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.11.2014
 *         Time: 7:20
 */
public class TableBrowserEditableView<W extends TableBrowserEditableComposite> extends Composite {
    private Panel root;
    private W mainWidget;

    public TableBrowserEditableView() {
        root = new HorizontalPanel();
        root.addStyleName("tableBrowserRoot");
        initWidget(root);
    }

    public void addWidget(Widget widget) {
        root.add(widget);
    }

    public void addMainWidget(W mainWidget) {
        this.mainWidget = mainWidget;
        root.add(mainWidget);
    }

    public W getMainWidget() {
        return mainWidget;
    }

    public String getFilterValue() {
        return mainWidget.getFilterValue();
    }

    public void clearContent() {
        mainWidget.clearContent();
    }

}
