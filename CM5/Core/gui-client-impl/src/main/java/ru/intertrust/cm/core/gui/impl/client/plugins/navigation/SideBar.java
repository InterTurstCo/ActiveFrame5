package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import ru.intertrust.cm.core.gui.impl.client.panel.RootNodeButton;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;

public class SideBar {

    public SideBar(SidebarView sideBarView) {
        this.sideBarView = sideBarView;
    }

    private SidebarView sideBarView;

    public void invoke() {

        RootNodeButton my1 = new RootNodeButton("Inbox");
        RootNodeButton my2 = new RootNodeButton("Tasks");
        RootNodeButton my3 = new RootNodeButton("Calendar");
        RootNodeButton my4 = new RootNodeButton("Docs");
        RootNodeButton my5 = new RootNodeButton("Discussion");
        RootNodeButton my6 = new RootNodeButton("Helpers");
        RootNodeButton my7 = new RootNodeButton("Cases");
        RootNodeButton my8 = new RootNodeButton("Analitika");

        sideBarView.sidebarItem("images/inbox.png", "Inbox", "Mail", 3587L, my1);

        sideBarView.sidebarItem("images/tasks.png", "Tasks", "Tasks", 555L, my2);

        sideBarView.sidebarItem("images/calendar.png", "Calendar", "Calendar", 0L, my3);

        sideBarView.sidebarItem("images/docs.png", "Docs", "Docs", 0L, my4);

        sideBarView.sidebarItem("images/discussions.png", "Discussion", "Discussion", 0L, my5);

        sideBarView.sidebarItem("images/helpers.png", "Helpers", "Helpers", 0L, my6);

        sideBarView.sidebarItem("images/cases.png", "Cases", "Cases", 0L, my7);

        sideBarView.sidebarItem("images/analitika.png", "Analitika", "Analitica", 0L, my8);

        sideBarView.getMenuItems().add(my1);
        sideBarView.getMenuItems().add(my2);
        sideBarView.getMenuItems().add(my3);
        sideBarView.getMenuItems().add(my4);
        sideBarView.getMenuItems().add(my5);
        sideBarView.getMenuItems().add(my6);
        sideBarView.getMenuItems().add(my7);
        sideBarView.getMenuItems().add(my8);
    }

}
