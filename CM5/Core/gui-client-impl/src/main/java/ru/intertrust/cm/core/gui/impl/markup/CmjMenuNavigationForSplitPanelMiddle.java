package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class CmjMenuNavigationForSplitPanelMiddle extends HorizontalPanel {
    public CmjMenuNavigationForSplitPanelMiddle() {
        Label labelTask = new Label("Задачи");
        Label labelInput = new Label("Поступившие");
        Label labelUnproc = new Label("Необработанные");
        this.add(labelTask);
        this.add(labelInput);
        this.add(labelUnproc);
    }
}
