package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjMenuNavigationDownSectionTabTableTwoContent extends HorizontalPanel {
    public CmjMenuNavigationDownSectionTabTableTwoContent() {

        VerticalPanel verticalPanelForTab = new VerticalPanel();
        VerticalPanel verticalPanelTable = new VerticalPanel();
        verticalPanelForTab.setWidth("235px");
        Label tabLabelFirst = new Label("Согласование");
        Label tabLabelSecond = new Label("Подписание");
        Label tabLabelThird = new Label("Регистрация");
        Label tabLabelFourth = new Label("Заверение");
        Label tabLabelFifth = new Label("Ознакомление");
        Label tabLabelSixth = new Label("Исполнение");
        verticalPanelForTab.add(tabLabelFirst);
        verticalPanelForTab.add(tabLabelSecond);
        verticalPanelForTab.add(tabLabelThird);
        verticalPanelForTab.add(tabLabelFourth);
        verticalPanelForTab.add(tabLabelFifth);
        verticalPanelForTab.add(tabLabelSixth);

        HorizontalPanel horizontalPanelHeader = new HorizontalPanel();

        Image textImgRefresh = new Image("css/icons/ico-reload.gif");
        Label textLabelRefresh = new Label("Обновить");
        horizontalPanelHeader.setWidth("100%");

        horizontalPanelHeader.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        horizontalPanelHeader.add(textImgRefresh);
        horizontalPanelHeader.add(textLabelRefresh);
        horizontalPanelHeader.setCellWidth(textLabelRefresh, "5px");
        verticalPanelTable.add(horizontalPanelHeader);
        verticalPanelTable.add(new CmjMenuNavigationForSplitPanelTable());
        this.add(verticalPanelForTab);
        this.add(verticalPanelTable);

    }
}
