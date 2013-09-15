package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 10.09.13
 *         Time: 18:58
 */
@Root(name = "hiding-groups")
public class HidingGroupListConfig extends TabGroupListConfig {
    @Attribute(name = "name", required = false)
    private String name;

    @ElementList(inline = true)
    private List<TabGroupConfig> tabGroupConfigs = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

        HidingGroupListConfig that = (HidingGroupListConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (tabGroupConfigs != null ? !tabGroupConfigs.equals(that.tabGroupConfigs) : that.tabGroupConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tabGroupConfigs != null ? tabGroupConfigs.hashCode() : 0);
        return result;
    }
}
