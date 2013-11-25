package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:00
 */
@Root(name = "body")
public class BodyConfig implements Dto {
    @Attribute(name = "display-single-tab")
    private boolean displaySingleTab;

    @ElementList(inline = true)
    private List<TabConfig> tabs = new ArrayList<TabConfig>();

    public boolean isDisplaySingleTab() {
        return displaySingleTab;
    }

    public void setDisplaySingleTab(boolean displaySingleTab) {
        this.displaySingleTab = displaySingleTab;
    }

    public List<TabConfig> getTabs() {
        return tabs;
    }

    public void setTabs(List<TabConfig> tabs) {
        this.tabs = tabs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BodyConfig that = (BodyConfig) o;

        if (displaySingleTab != that.displaySingleTab) {
            return false;
        }
        if (tabs != null ? !tabs.equals(that.tabs) : that.tabs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (displaySingleTab ? 1 : 0);
        result = 31 * result + (tabs != null ? tabs.hashCode() : 0);
        return result;
    }
}
