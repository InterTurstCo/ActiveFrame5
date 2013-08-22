package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;

public class CmjMenuNavigationDownSectionTab extends TabPanel {
    public CmjMenuNavigationDownSectionTab() {
        this.add(new HorizontalPanel(), "Содержание");
        this.add(new HorizontalPanel(), "Реквизиты");
        this.add(new HorizontalPanel(), "Жизненный цикл");
        this.add(new CmjMenuNavigationDownSectionTabTableTwoContent(), "Связанные");
        this.add(new HorizontalPanel(), "Обсуждения");
        this.add(new CmjMenuNavigationForSplitPanelTable(), "История");

        this.selectTab(3);
    }
}
