package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.gui.navigation.PluginConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:06
 */
@Root(name = "single-entry-group")
public class SingleEntryGroupListConfig extends TabGroupListConfig {
    @Element(name = "table")
    private TabGroupConfig tabGroupConfig;

    public TabGroupConfig getTabGroupConfig() {
        return tabGroupConfig;
    }

    public void setTabGroupConfig(TabGroupConfig tabGroupConfig) {
        this.tabGroupConfig = tabGroupConfig;
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

        if (tabGroupConfig != null ? !tabGroupConfig.equals(that.tabGroupConfig) : that.tabGroupConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabGroupConfig != null ? tabGroupConfig.hashCode() : 0;
    }
}
