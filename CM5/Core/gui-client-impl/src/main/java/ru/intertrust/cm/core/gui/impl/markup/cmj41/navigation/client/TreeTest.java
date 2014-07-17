package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client;

import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.component.tree.systemTree.resources.SystemTreeStyles;
import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.module.root.view.sidebar.SidebarView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;


public class TreeTest {//implements EntryPoint {
/*

    interface MyTreeImages extends TreeImages {

        @Resource("treeOpen.png")
        AbstractImagePrototype treeOpen();

        @Resource("treeClosed.png")
        AbstractImagePrototype treeClosed();
      }





    public static int WIDTH = 190;

    public boolean isReadedData = true;

    TreeItem activeMenu;
    TreeItem currentActiveItem;

    Tree tree; //= new Tree();
    SidebarView s = new SidebarView();
    HorizontalPanel horizontalPanel = new HorizontalPanel();

    private void setHorizontalPanelStyle(){
        horizontalPanel.getElement().getStyle().setProperty("backgroundColor", "blue");
        horizontalPanel.getElement().getStyle().setColor("white");

    }

    public void initComponent(){
        setHorizontalPanelStyle();
        createSideBar();

        createTree();



        horizontalPanel.add(s);
        horizontalPanel.add(tree);

        RootPanel.get().add(horizontalPanel);

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
    }


    private void createSideBar() {
        MyButton my1 = new MyButton("Inbox");
        MyButton my2 = new MyButton("Tasks");
        MyButton my3 = new MyButton("Calendar");
        MyButton my4 = new MyButton("Docs");
        MyButton my5 = new MyButton("Discussion");
        MyButton my6 = new MyButton("Helpers");
        MyButton my7 = new MyButton("Cases");
        MyButton my8 = new MyButton("Analitika");

        s.sidebarItem("images/inbox.png", "Inbox", "Mail", 3587L, my1);

        s.sidebarItem("images/tasks.png", "Tasks", "Tasks", 555L, my2);

        s.sidebarItem("images/calendar.png", "Calendar", "Calendar", 0L, my3);

        s.sidebarItem("images/docs.png", "Docs", "Docs", 0L, my4);

        s.sidebarItem("images/discussions.png", "Discussion", "Discussion", 0L, my5);

        s.sidebarItem("images/helpers.png", "Helpers", "Helpers", 0L, my6);

        s.sidebarItem("images/cases.png", "Cases", "Cases", 0L, my7);

        s.sidebarItem("images/analitika.png", "Analitika", "Analitica", 0L, my8);

        s.getMenuItems().add(my1);
        s.getMenuItems().add(my2);
        s.getMenuItems().add(my3);
        s.getMenuItems().add(my4);
        s.getMenuItems().add(my5);
        s.getMenuItems().add(my6);
        s.getMenuItems().add(my7);
        s.getMenuItems().add(my8);
    }

    @SuppressWarnings("deprecation")
    private void createTree() {
        NavigationTreeStyles.I.styles().ensureInjected();
        @SuppressWarnings("deprecation")
        TreeImages images = GWT.create(MyTreeImages.class);

        tree = new Tree(images);



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

        TreeItem i = new TreeItem(selected("some selected", 0));

        tree.addItem(i);

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
        }
        else {
            return "";
        }
    }

    public HorizontalPanel getHorizontalPanel() {
        return horizontalPanel;
    }

    public void setHorizontalPanel(HorizontalPanel horizontalPanel) {
        this.horizontalPanel = horizontalPanel;
    }*/
}
