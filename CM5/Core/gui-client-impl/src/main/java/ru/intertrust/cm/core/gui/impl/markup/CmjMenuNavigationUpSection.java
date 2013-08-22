package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjMenuNavigationUpSection extends VerticalPanel {
    public CmjMenuNavigationUpSection() {
        this.add(new CmjMenuNavigationForSplitPanelUp());
        this.add(new CmjMenuNavigationForSplitPanelMiddle());
        this.add(new CmjMenuNavigationForSplitPanelTable());
    }
}
