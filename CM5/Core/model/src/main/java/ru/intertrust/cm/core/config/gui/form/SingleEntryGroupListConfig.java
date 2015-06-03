package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:06
 */
@Root(name = "single-entry-group")
public class SingleEntryGroupListConfig extends TabGroupListConfig {
    @ElementList(name = "tab-group", inline = true)
    private List<TabGroupConfig> tabGroupConfigs = new ArrayList<>();

    public TabGroupConfig getTabGroupConfig() {
        return tabGroupConfigs.isEmpty() ? null : tabGroupConfigs.get(0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleEntryGroupListConfig that = (SingleEntryGroupListConfig) o;

        if (tabGroupConfigs != null ? !tabGroupConfigs.equals(that.tabGroupConfigs) : that.tabGroupConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabGroupConfigs != null ? tabGroupConfigs.hashCode() : 0;
    }

    @Override
    public List<TabGroupConfig> getTabGroupConfigs() {
        return tabGroupConfigs;
    }
}
