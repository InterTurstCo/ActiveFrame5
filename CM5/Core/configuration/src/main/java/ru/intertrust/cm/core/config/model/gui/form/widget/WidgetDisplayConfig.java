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
public class WidgetDisplayConfig implements Dto {
    @Attribute(name = "id")
    private String id;

    private String width;

    private String height;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetDisplayConfig that = (WidgetDisplayConfig) o;

        if (height != null ? !height.equals(that.height) : that.height != null) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (width != null ? !width.equals(that.width) : that.width != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (width != null ? width.hashCode() : 0);
        result = 31 * result + (height != null ? height.hashCode() : 0);
        return result;
    }
}
