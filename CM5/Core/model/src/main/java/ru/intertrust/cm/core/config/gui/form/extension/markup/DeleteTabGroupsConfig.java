package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:29
 */
@Root(name = "delete-tab-groups")
public class DeleteTabGroupsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "tab-group")
    private List<TabGroupConfig> tabGroupConfigs = new ArrayList<TabGroupConfig>();

    public List<TabGroupConfig> getTabGroupConfigs() {
        return tabGroupConfigs;
    }

    public void setTabGroupConfigs(List<TabGroupConfig> tabGroups) {
        this.tabGroupConfigs = tabGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteTabGroupsConfig that = (DeleteTabGroupsConfig) o;

        if (tabGroupConfigs != null ? !tabGroupConfigs.equals(that.tabGroupConfigs) : that.tabGroupConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabGroupConfigs != null ? tabGroupConfigs.hashCode() : 0;
    }

}
