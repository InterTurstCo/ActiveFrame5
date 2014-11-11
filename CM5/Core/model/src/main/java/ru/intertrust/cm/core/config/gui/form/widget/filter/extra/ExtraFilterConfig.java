package ru.intertrust.cm.core.config.gui.form.widget.filter.extra;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ExtraParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.NullFilterConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.11.2014
 *         Time: 10:25
 */
@Root(name = "extra-filter")
public class ExtraFilterConfig extends NullFilterConfig<ExtraParamConfig> {
    @ElementList(name = "param", inline = true, required = false)
    private List<ExtraParamConfig> paramConfigs;

    public List<ExtraParamConfig> getParamConfigs() {
        return paramConfigs;
    }

    public void setParamConfigs(List<ExtraParamConfig> paramConfigs) {
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

        ExtraFilterConfig that = (ExtraFilterConfig) o;

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
