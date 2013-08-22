package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class CmjMenuNavigationDownSectionUp extends HorizontalPanel {
    public CmjMenuNavigationDownSectionUp() {
        this.add(new Image("css/icons/ico-progress.gif"));
        this.add(new Label("Внутренний"));
        this.add(new Image("css/icons/ico-progress.gif"));
        this.add(new Label("Служебная записка"));
    }
}
