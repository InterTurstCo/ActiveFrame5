package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "initial-filter")
public class InitialFilterConfig implements Dto {
    @Attribute(name = "name")
    private String name;

    @ElementList(inline = true, name ="param", required = false)
    private List<ParamConfig> paramConfigs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamConfig> getParamConfigs() {
        return paramConfigs;
    }

    public void setParamConfigs(List<ParamConfig> paramConfigs) {
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

        InitialFilterConfig that = (InitialFilterConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (paramConfigs != null ? !paramConfigs.equals(that.paramConfigs) : that.paramConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (paramConfigs != null ? paramConfigs.hashCode() : 0);
        return result;
    }
}
