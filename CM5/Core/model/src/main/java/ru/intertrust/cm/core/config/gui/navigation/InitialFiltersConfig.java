package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "initial-filters")
public class InitialFiltersConfig extends AbstractFiltersConfig implements Dto {
    @Attribute(name = "panel-state", required = false)
    private String panelState;

    public String getPanelState() {
        return panelState;
    }

    public void setPanelState(String panelState) {
        this.panelState = panelState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InitialFiltersConfig)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        InitialFiltersConfig that = (InitialFiltersConfig) o;

        if (panelState != null ? !panelState.equals(that.panelState) : that.panelState != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (panelState != null ? panelState.hashCode() : 0);
        return result;
    }
}
