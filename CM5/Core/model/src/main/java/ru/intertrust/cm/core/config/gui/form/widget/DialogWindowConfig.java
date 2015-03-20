package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 30.01.2014
 *         Time: 01:56:53
 */
@Root(name="dialog-window")
public class DialogWindowConfig implements Dto{

    @Attribute(name="width")
    private String width;

    @Attribute(name="height")
    private String height;

    @Attribute(name = "resizable", required = false)
    private boolean resizable;

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

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DialogWindowConfig that = (DialogWindowConfig) o;
        if (height != null ? !height.equals(that.height) : that.height != null) {
            return false;
        }
        if (width != null ? !width.equals(that.width) : that.width != null) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = width != null ? width.hashCode() : 0;
        result = 31 * result + (height != null ? height.hashCode() : 0);
        result = 31 * result + (resizable ? 1 : 0);
        return result;
    }
}
