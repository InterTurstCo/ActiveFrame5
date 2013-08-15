package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.entity.sidebar;

import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.module.root.view.sidebar.SidebarView;

import com.google.gwt.user.client.ui.HTML;

public class MyButton extends HTML {

    private HTML html;
    private String nameName;

    private final SidebarView sidebar = new SidebarView();

    public MyButton(String nameName) {

        this.html = sidebar.getSidebarItem();

        this.sidebar.putNavigationMap(nameName, html);

    }

    public HTML getHtml() {
        return html;
    }

    public void setHtml(HTML html) {
        this.html = html;
    }

    public String getNameName() {
        return nameName;
    }

    public void setNameName(String nameName) {
        this.nameName = nameName;
    }

}
