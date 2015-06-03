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
 *         Time: 10:53
 */
@Root(name = "replace-tabs")
public class ReplaceTabsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "replace-tabs")
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

        ReplaceTabsConfig that = (ReplaceTabsConfig) o;

        if (tabConfigs != null ? !tabConfigs.equals(that.tabConfigs) : that.tabConfigs != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tabConfigs != null ? tabConfigs.hashCode() : 0;
    }

}
