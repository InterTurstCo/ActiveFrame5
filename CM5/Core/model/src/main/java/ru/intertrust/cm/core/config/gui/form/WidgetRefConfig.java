package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

/**
 * @author Lesia Puhova
 *         Date: 02.09.14
 *         Time: 18:58
 */
@Root(name="widget-ref")
public class WidgetRefConfig implements IdentifiedConfig {

    @Attribute(name="id")
    private String id;

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WidgetRefConfig that = (WidgetRefConfig) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
