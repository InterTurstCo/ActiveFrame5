package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "filter-panel")
public class FilterPanelConfig implements Dto {
    @Attribute(name = "panel-state", required = true)
    private String panelState;

    public String getPanelState() {
        return panelState;
    }

    public void setPanelState(String panelState) {
        this.panelState = panelState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FilterPanelConfig that = (FilterPanelConfig) o;

        if (panelState != null ? !panelState.equals(that.panelState) : that.panelState != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return panelState != null ? panelState.hashCode() : 0;
    }
}
