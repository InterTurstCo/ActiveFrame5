package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class ItemViewerPluginView extends PluginView {
    public ItemViewerPluginView(Plugin itemViewerPlugin) {
        super(itemViewerPlugin);
    }

    @Override
    protected IsWidget getViewWidget() {
        VerticalPanel container = new VerticalPanel();
        container.getElement().getStyle().setProperty("backgroundColor", "#EEE");

        HorizontalPanel navigationDownSectionUp = new HorizontalPanel();

        navigationDownSectionUp.add(new Image("css/icons/ico-progress.gif"));
        navigationDownSectionUp.add(new Label("Внутренний"));
        navigationDownSectionUp.add(new Image("css/icons/ico-progress.gif"));
        navigationDownSectionUp.add(new Label("Служебная записка"));

        HorizontalPanel navigationSectionText = new HorizontalPanel();
        navigationSectionText.add(new Label("das"));

        HorizontalPanel navigationSectionMiddle = new HorizontalPanel();

        navigationSectionMiddle.add(new Label("Проект на согласовании"));
        navigationSectionMiddle.add(new Label("Подпись:"));
        navigationSectionMiddle.add(new Label("Болотов Р. В."));
        navigationSectionMiddle.add(new Image("css/icons/ico-cert.gif"));
        navigationSectionMiddle.add(new Image("css/icons/clokring.png"));
        navigationSectionMiddle.add(new Label("Срок:"));
        navigationSectionMiddle.add(new Label("не задан"));
        navigationSectionMiddle.add(new Button("нет контроля"));
        container.add(navigationSectionMiddle);

        TabPanel tabPanel = new TabPanel();

        tabPanel.add(new HorizontalPanel(), "Содержание");
        tabPanel.add(new HorizontalPanel(), "Реквизиты");
        tabPanel.add(new HorizontalPanel(), "Жизненный цикл");

        HorizontalPanel tabTableTwoContent = new HorizontalPanel();

        VerticalPanel verticalPanelForTab = new VerticalPanel();
        VerticalPanel verticalPanelTable = new VerticalPanel();
        verticalPanelForTab.setWidth("230px");
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
        verticalPanelTable.add(new HorizontalPanel());
        tabTableTwoContent.add(verticalPanelForTab);
        tabTableTwoContent.add(verticalPanelTable);

        tabPanel.add(tabTableTwoContent, "Связанные");
        tabPanel.add(new HorizontalPanel(), "Обсуждения");
        tabPanel.add(new HorizontalPanel(), "История");

        tabPanel.selectTab(3);
        container.add(tabPanel);

        return container;
    }
}
