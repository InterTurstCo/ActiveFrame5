package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "initial-filters")
public class InitialFiltersConfig implements Dto {
    @Attribute(name = "panel-state", required = false)
    private String panelState;

    @ElementList(inline = true, name ="initial-filter", required = true)
    private List<InitialFilterConfig> initialFilterConfigs;

    public String getPanelState() {
        return panelState;
    }

    public void setPanelState(String panelState) {
        this.panelState = panelState;
    }

    public List<InitialFilterConfig> getInitialFilterConfigs() {
        return initialFilterConfigs;
    }

    public void setInitialFilterConfigs(List<InitialFilterConfig> initialFilterConfigs) {
        this.initialFilterConfigs = initialFilterConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InitialFiltersConfig that = (InitialFiltersConfig) o;

        if (panelState != null ? !panelState.equals(that.panelState) : that.panelState != null) {
            return false;
        }

        if (initialFilterConfigs != null ? !initialFilterConfigs.equals(that.initialFilterConfigs) :
                that.initialFilterConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = panelState != null ? panelState.hashCode() : 0;
        result = 31 * result + (initialFilterConfigs != null ? initialFilterConfigs.hashCode() : 0);
        return result;
    }
}
