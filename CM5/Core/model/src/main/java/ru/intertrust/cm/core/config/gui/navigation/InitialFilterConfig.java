package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "initial-filter")
public class InitialFilterConfig extends AbstractFilterConfig<InitialParamConfig> implements Dto {

    @ElementList(name = "param", inline = true, required = false)
    private List<InitialParamConfig> paramConfigs;

    public List<InitialParamConfig> getParamConfigs() {
        return paramConfigs;
    }

    public void setParamConfigs(List<InitialParamConfig> paramConfigs) {
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

        InitialFilterConfig that = (InitialFilterConfig) o;

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
