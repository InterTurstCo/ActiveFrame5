package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 19:00
 */
@Root(name="map-provider")
public class EnumMapProviderConfig implements Dto {

    @Attribute
    private String component;

    public String getComponent() {
        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EnumMapProviderConfig that = (EnumMapProviderConfig) o;

        if (!component.equals(that.component)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }
}
