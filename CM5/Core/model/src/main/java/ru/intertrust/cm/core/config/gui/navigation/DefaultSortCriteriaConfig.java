package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 20/9/13
 *         Time: 12:05 PM
 */
@Root(name = "default-sort-criteria")
public class DefaultSortCriteriaConfig extends CommonSortCriterionConfig {
    @Attribute(name = "column-field", required = false)
    private String columnField;

    public String getColumnField() {
        return columnField;
    }

    public void setColumnField(String columnField) {
        this.columnField = columnField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) return false;

        DefaultSortCriteriaConfig that = (DefaultSortCriteriaConfig) o;

        if (columnField != null ? !columnField.equals(that.columnField) : that.columnField != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (columnField != null ? columnField.hashCode() : 0);
        return result;
    }
}
