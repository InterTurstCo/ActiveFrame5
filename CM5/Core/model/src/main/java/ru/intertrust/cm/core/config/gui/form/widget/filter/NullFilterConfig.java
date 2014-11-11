package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Attribute;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.11.2014
 *         Time: 10:27
 */
public abstract class NullFilterConfig<T extends ParamConfig> extends AbstractFilterConfig<T>{
    @Attribute(name = "null-value-filter-name", required = false)
    private String nullValueFilterName;

    public String getNullValueFilterName() {
        return nullValueFilterName;
    }

    public void setNullValueFilterName(String nullValueFilterName) {
        this.nullValueFilterName = nullValueFilterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NullFilterConfig that = (NullFilterConfig) o;

        if (nullValueFilterName != null ? !nullValueFilterName.equals(that.nullValueFilterName)
                : that.nullValueFilterName != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nullValueFilterName != null ? nullValueFilterName.hashCode() : 0);
        return result;
    }
}
