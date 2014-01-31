package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "sort-criterion")
public class SortCriterionConfig extends CommonSortCriterionConfig {
    @Attribute(name = "field", required = false)
    private String field;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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

        SortCriterionConfig that = (SortCriterionConfig) o;

        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }
}
