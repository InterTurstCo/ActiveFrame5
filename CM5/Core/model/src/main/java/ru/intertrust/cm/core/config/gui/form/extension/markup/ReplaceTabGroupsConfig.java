package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.05.2015
 *         Time: 21:01
 */
@Root(name = "replace-tab-groups")
public class ReplaceTabGroupsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "tab-group")
    private List<TabGroupConfig> tabGroupConfigs = new ArrayList<TabGroupConfig>();

    public List<TabGroupConfig> getTabGroupConfigs() {
        return tabGroupConfigs;
    }

    public void setTabGroupConfigs(List<TabGroupConfig> tabGroupConfigs) {
        this.tabGroupConfigs = tabGroupConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReplaceTabGroupsConfig that = (ReplaceTabGroupsConfig) o;

        if (tabGroupConfigs != null ? !tabGroupConfigs.equals(that.tabGroupConfigs) : that.tabGroupConfigs != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabGroupConfigs != null ? tabGroupConfigs.hashCode() : 0;
    }

}
