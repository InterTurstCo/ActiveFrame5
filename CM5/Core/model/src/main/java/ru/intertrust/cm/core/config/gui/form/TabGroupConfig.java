package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 10.09.13
 *         Time: 19:04
 */
@Root(name = "tab-group")
public class TabGroupConfig implements IdentifiedConfig {
    @Attribute(name = "name", required = false)
    @Localizable
    private String name;

    @Attribute(name = "initial-state", required = false)
    private String initialState;

    @Attribute(name = "id", required = false)
    private String id;

    @Element(name = "table", required = false)
    private TableLayoutConfig tableLayout;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitialState() {
        return initialState;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TableLayoutConfig getLayout() {
        return tableLayout;
    }

    public void setLayout(TableLayoutConfig tableLayout) {
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
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (tableLayout != null ? !tableLayout.equals(that.tableLayout) : that.tableLayout != null) {
            return false;
        }
        if (initialState != null ? !initialState.equals(that.initialState) : that.initialState != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (tableLayout != null ? tableLayout.hashCode() : 0);
        result = 31 * result + (initialState != null ? initialState.hashCode() : 0);
        return result;
    }
}
