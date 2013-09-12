package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:00
 */
public class BodyConfig implements Dto {
    private List<TabConfig> tabs;

    public List<TabConfig> getTabs() {
        return tabs;
    }

    public void setTabs(List<TabConfig> tabs) {
        this.tabs = tabs;
    }
}
