package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjHeader extends VerticalPanel {
    public CmjHeader() {
        this.add(new CmjPreHeader());
        this.add(new CmjMenubar());
        this.setWidth("100%");

    }
}
