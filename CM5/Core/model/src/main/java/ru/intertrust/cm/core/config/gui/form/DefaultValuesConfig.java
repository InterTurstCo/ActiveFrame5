package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 02.10.14.
 */
@Root(name = "default-values")
public class DefaultValuesConfig implements Dto {

    @ElementList(inline = true, entry = "default-value")
    List<DefaultValueConfig> defaultValueConfigs = new ArrayList<>();

    public List<DefaultValueConfig> getDefaultValueConfigs() {
        return defaultValueConfigs;
    }

    public void setDefaultValueConfigs(List<DefaultValueConfig> defaultValueConfigs) {
        this.defaultValueConfigs = defaultValueConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultValuesConfig that = (DefaultValuesConfig) o;

        if (!defaultValueConfigs.equals(that.defaultValueConfigs)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return defaultValueConfigs.hashCode();
    }
}
