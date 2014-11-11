package ru.intertrust.cm.core.config.gui.form.widget.filter;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "selection-filter")
public class SelectionFilterConfig extends NullFilterConfig<SelectionParamConfig> {
    @ElementList(name = "param", inline = true, required = false)
    private List<SelectionParamConfig> paramConfigs;

    public List<SelectionParamConfig> getParamConfigs() {
        return paramConfigs;
    }

    public void setParamConfigs(List<SelectionParamConfig> paramConfigs) {
        this.paramConfigs = paramConfigs;
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

        SelectionFilterConfig that = (SelectionFilterConfig) o;

        if (paramConfigs != null ? !paramConfigs.equals(that.paramConfigs) : that.paramConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (paramConfigs != null ? paramConfigs.hashCode() : 0);
        return result;
    }
}
