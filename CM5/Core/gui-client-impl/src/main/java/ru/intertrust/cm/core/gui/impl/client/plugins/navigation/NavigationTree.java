package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.panel.RootNodeButton;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.panel.SystemTreeStyles;

public class NavigationTree extends HorizontalPanel {

    TreeItem currentActiveItem;

    interface MyTreeImages extends TreeImages {
        @Resource("treeOpen.png")
        AbstractImagePrototype treeOpen();

        @Resource("treeClosed.png")
        AbstractImagePrototype treeClosed();
    }

    public NavigationTree() {

        SidebarView sideBarView = new SidebarView();
        this.getElement().getStyle().setColor("white");

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

        SystemTreeStyles.I.styles().ensureInjected();
        TreeImages images = GWT.create(MyTreeImages.class);

        Tree tree = new Tree(images);
        tree.setAnimationEnabled(true);
        tree.setStyleName("folder-list");

        TreeItem i1 = new TreeItem("Контроль Исполнения");

        TreeItem i2 = new TreeItem(textWrap("Отчеты ", 0, "tree-cell"));
        TreeItem i3 = new TreeItem(textWrap("Статистика использования", 0, "tree-cell"));

        TreeItem i11 = new TreeItem(textWrap("Не исполненное ", 0, "tree-cell"));
        TreeItem i12 = new TreeItem(textWrap("Исполненные", 0, "tree-cell"));

        TreeItem i111 = new TreeItem(textWrap("По Исполнителю", 0, "tree-cell"));
        TreeItem i112 = new TreeItem(textWrap("По Автору ", 0, "tree-cell"));
        TreeItem i113 = new TreeItem(textWrap("По номеру документа ", 0, "tree-cell"));

        TreeItem i121 = new TreeItem(textWrap("По Исполнителю", 0, "tree-cell"));
        TreeItem i122 = new TreeItem(textWrap("По Автору ", 0, "tree-cell"));
        TreeItem i123 = new TreeItem(textWrap("По номеру документа ", 0, "tree-cell"));


        tree.addItem(i1);
        tree.addItem(i2);
        tree.addItem(i3);

        i1.addItem(i11);
        i1.addItem(i12);

        i11.addItem(i111);
        i11.addItem(i112);
        i11.addItem(i113);

        i12.addItem(i121);
        i12.addItem(i122);
        i12.addItem(i123);

        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                if (currentActiveItem != null && currentActiveItem != event.getSelectedItem()) {
                    currentActiveItem.removeStyleName("synchronized");
                    currentActiveItem.setStyleName("tree-cell");
                }
                currentActiveItem = event.getSelectedItem();
                event.getSelectedItem().setStyleName("synchronized");
            }
        });

        this.add(sideBarView);
        this.add(tree);
    }

    public String selected(String text, int counter) {
        return "<div class='fl-selected'>" + text
                + "<div class='fl-arrow-left'></div><div class='fl-arrow-right'></div>" + counter(counter) + "</div>";
    }

    public String textWrap(String text, int counter, String cssStyleName) {
        return "<span>" + text + "</span>" + counter(counter);
    }

    public String counter(int num) {
        if (num > 0) {
            return "<div class='fl-counter'>" + num + "</div>";
        } else {
            return "";
        }
    }

}
