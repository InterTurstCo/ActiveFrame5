package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 18:54
 */
@Root(name = "mapping")
public class EnumMappingConfig implements Dto {

    @ElementList(inline = true)
    List<EnumMapConfig> enumMapConfigs = new ArrayList<EnumMapConfig>();

    public List<EnumMapConfig> getEnumMapConfigs() {
        return enumMapConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnumMappingConfig that = (EnumMappingConfig) o;

        if (!enumMapConfigs.equals(that.enumMapConfigs)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return enumMapConfigs.hashCode();
    }
}
