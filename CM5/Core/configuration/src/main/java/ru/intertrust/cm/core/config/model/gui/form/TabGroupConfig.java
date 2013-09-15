package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Denis Mitavskiy
 *         Date: 10.09.13
 *         Time: 19:04
 */
@Root(name = "group")
public class TabGroupConfig {
    @Attribute(name = "name", required = false)
    private String name;

    @Element(name = "table")
    private LayoutConfig tableLayout;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LayoutConfig getLayout() {
        return tableLayout;
    }

    public void setLayout(LayoutConfig tableLayout) {
        this.tableLayout = tableLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TabGroupConfig that = (TabGroupConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (tableLayout != null ? !tableLayout.equals(that.tableLayout) : that.tableLayout != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tableLayout != null ? tableLayout.hashCode() : 0);
        return result;
    }
}
