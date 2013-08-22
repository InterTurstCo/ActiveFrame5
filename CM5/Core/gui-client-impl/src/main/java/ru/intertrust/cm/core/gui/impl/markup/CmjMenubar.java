package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;

public class CmjMenubar extends DockLayoutPanel {

    public CmjMenubar() {
        super(Unit.EM);
        addContentMainHeader();
    }

    void addContentMainHeader() {

        Button createBtn = new Button("Создать");
        Image treeBtnShow = new Image("css/images/icon-folderlist.png");

        Command cmd = new Command() {
            public void execute() {
                //
            }
        };

        MenuBar allMenu = new MenuBar(true);
        allMenu.addItem("Все", cmd);
        allMenu.addItem("Все", cmd);
        allMenu.addItem("Все", cmd);

        MenuBar notreadMenu = new MenuBar(true);
        notreadMenu.addItem("Непрочтенные", cmd);
        notreadMenu.addItem("Непрочтенные", cmd);
        notreadMenu.addItem("Непрочтенные", cmd);

        MenuBar recMenu = new MenuBar(true);
        recMenu.addItem("Корзина", cmd);
        recMenu.addItem("Корзина", cmd);
        recMenu.addItem("Корзина", cmd);

        MenuBar menu = new MenuBar();
        menu.addItem("Все", allMenu);
        menu.addItem("Непрочтенные", notreadMenu);
        menu.addItem("Корзина", recMenu);

        Image positionForSplitPanelH = new Image("css/images/btn-verthor2.png");
        Image positionForSplitPanelV = new Image("css/images/btn-verthor2.png");
        Image showStickerPanel = new Image("css/images/icon-folderlist.png");

        this.addWest(createBtn, 15);
        this.addWest(treeBtnShow, 4);
        this.addEast(showStickerPanel, 4);

        this.addEast(positionForSplitPanelH, 2.3);
        this.addEast(positionForSplitPanelV, 2.3);

        this.add(menu);
        this.setHeight("30px");
    }

}
