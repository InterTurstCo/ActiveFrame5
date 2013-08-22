package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjMenuNavigationDownSection extends VerticalPanel {

    public CmjMenuNavigationDownSection() {
        this.add(new CmjMenuNavigationDownSectionUp());
        this.add(new CmjMenuNavigationDownSectionText());
        this.add(new CmjMenuNavigationDownSectionMiddle());
        this.add(new CmjMenuNavigationDownSectionTab());
    }
}
