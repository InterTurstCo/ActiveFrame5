package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:00
 */
@Root(name = "header")
public class HeaderConfig implements Dto {
    @Element(name = "table")
    private TableLayoutConfig tableLayout;

    public TableLayoutConfig getTableLayout() {
        return tableLayout;
    }

    public void setTableLayout(TableLayoutConfig tableLayout) {
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

        HeaderConfig that = (HeaderConfig) o;

        if (tableLayout != null ? !tableLayout.equals(that.getTableLayout()) : that.getTableLayout() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tableLayout != null ? tableLayout.hashCode() : 0;
    }
}
