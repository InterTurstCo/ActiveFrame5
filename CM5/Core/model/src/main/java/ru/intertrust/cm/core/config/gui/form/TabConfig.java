package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:02
 */
@Root(name = "tab")
public class TabConfig implements IdentifiedConfig, TabConfigMarker {
    @Attribute(name = "name", required = false)
    @Localizable
    private String name;

    @Attribute(name = "id", required = false)
    private String id;

    @ElementUnion({
            @Element(name="bookmarks", type=BookmarkListConfig.class, required = false),
            @Element(name="hiding-groups", type=HidingGroupListConfig.class, required = false),
            @Element(name="single-entry-group", type=SingleEntryGroupListConfig.class, required = false)
    })
    private TabGroupListConfig groupList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TabGroupListConfig getGroupList() {
        return groupList;
    }

    public void setGroupList(TabGroupListConfig groupList) {
        this.groupList = groupList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TabConfig tabConfig = (TabConfig) o;

        if (groupList != null ? !groupList.equals(tabConfig.groupList) : tabConfig.groupList != null) {
            return false;
        }
        if (name != null ? !name.equals(tabConfig.name) : tabConfig.name != null) {
            return false;
        }
        if (id != null ? !id.equals(tabConfig.id) : tabConfig.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (groupList != null ? groupList.hashCode() : 0);
        return result;
    }
}
