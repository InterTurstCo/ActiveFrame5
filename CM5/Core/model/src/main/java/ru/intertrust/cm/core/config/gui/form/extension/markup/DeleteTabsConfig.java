package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:26
 */
@Root(name = "delete-tabs")
public class DeleteTabsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "tab")
    private List<TabConfig> tabConfigs = new ArrayList<TabConfig>();

    public List<TabConfig> getTabConfigs() {
        return tabConfigs;
    }

    public void setTabConfigs(List<TabConfig> tabConfigs) {
        this.tabConfigs = tabConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteTabsConfig that = (DeleteTabsConfig) o;

        if (tabConfigs != null ? !tabConfigs.equals(that.tabConfigs) : that.tabConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabConfigs != null ? tabConfigs.hashCode() : 0;
    }

}
