package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class CmjMenuNavigationForSplitPanelUp extends HorizontalPanel {
    public CmjMenuNavigationForSplitPanelUp() {
        HorizontalPanel menuNavigatin = new HorizontalPanel();

        Image imgRefresh = new Image("css/icons/ico-reload.gif");
        Label labelRefresh = new Label("Обновить");

        Image imgEdit = new Image("css/icons/ico-edit.gif");
        Label labelEdit = new Label("Редактировать");

        Image imgCreate = new Image("css/icons/icon-create.png");
        Label labelCreate = new Label("Создать");

        Label labelOther = new Label("Другое");

        Label labelSelect = new Label("Отметить");

        Image imgPlane = new Image("css/icons/icon-datepicker2.png");
        Label labelPlane = new Label("Запланировать");

        Label labelsignate = new Label("Назначить");

        this.add(imgRefresh);
        this.add(labelRefresh);

        this.add(imgCreate);
        this.add(labelCreate);

        this.add(labelOther);

        this.add(labelSelect);

        this.add(imgPlane);

        this.add(labelPlane);

        this.add(labelsignate);
    }
}
