package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 11.02.2015
 *         Time: 16:53
 */
@Root(name = "exact-types")
public class ExactTypesConfig implements Dto {
    @ElementList(entry = "exact-type", type = ExactTypeConfig.class, inline = true, required = true)
    private List<ExactTypeConfig> exactTypeConfigs = new ArrayList<>();

    public List<ExactTypeConfig> getExactTypeConfigs() {
        return exactTypeConfigs;
    }

    public void setExactTypeConfigs(List<ExactTypeConfig> exactTypeConfigs) {
        this.exactTypeConfigs = exactTypeConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExactTypesConfig that = (ExactTypesConfig) o;

        if (!exactTypeConfigs.equals(that.exactTypeConfigs)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return exactTypeConfigs.hashCode();
    }
}
