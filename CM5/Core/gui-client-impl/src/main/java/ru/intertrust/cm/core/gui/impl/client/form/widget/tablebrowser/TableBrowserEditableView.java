package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.user.client.ui.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.11.2014
 *         Time: 7:20
 */
public class TableBrowserEditableView extends Composite {
    private Panel header;
    private Panel root;

    public TableBrowserEditableView() {

        root = new VerticalPanel();
        root.addStyleName("tableBrowserRoot");
        header = new HorizontalPanel();
        root.add(header);
        initWidget(root);
    }

    public void addHeaderWidget(Widget widget) {
        header.add(widget);
    }

    public void addBodyWidget(Widget widget) {
        root.add(widget);
    }


}
