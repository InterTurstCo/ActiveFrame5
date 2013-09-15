package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Виджет - элемент пользовательского интерфейса, отобрающий специфическим образом некоторые данные в определённой части
 * разметки формы.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 14:52
 */
@Root(name = "widget")
public class CellWidgetConfig implements Dto {
    @Attribute(name = "id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellWidgetConfig that = (CellWidgetConfig) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
