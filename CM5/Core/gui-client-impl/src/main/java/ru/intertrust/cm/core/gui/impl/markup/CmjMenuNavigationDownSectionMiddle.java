package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class CmjMenuNavigationDownSectionMiddle extends HorizontalPanel {
    public CmjMenuNavigationDownSectionMiddle() {
        this.add(new Label("Проект на согласовании"));
        this.add(new Label("Подпись:"));
        this.add(new Label("Болотов Р. В."));
        this.add(new Image("css/icons/ico-cert.gif"));
        this.add(new Image("css/icons/clokring.png"));
        this.add(new Label("Срок:"));
        this.add(new Label("не задан"));
        this.add(new Button("нет контроля"));
    }
}
