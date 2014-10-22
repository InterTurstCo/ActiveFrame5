package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 12:56
 */
public class PreviewConfig implements Dto {

    @Attribute(name="display", required = false)
    private boolean display = true;

    @Attribute(name="width", required = false)
    private String width;

    @Attribute(name="height", required = false)
    private String height;

    @Attribute(name="preserve-proportion", required = false)
    private boolean preserveProportion;

    public boolean isDisplay() {
        return display;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public boolean isPreserveProportion() {
        return preserveProportion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreviewConfig that = (PreviewConfig) o;
        if (display != that.display) {
            return false;
        }
        if (preserveProportion != that.preserveProportion) {
            return false;
        }
        if (height != null ? !height.equals(that.height) : that.height != null) {
            return false;
        }
        if (width != null ? !width.equals(that.width) : that.width != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (display ? 1 : 0);
        result = 31 * result + (width != null ? width.hashCode() : 0);
        result = 31 * result + (height != null ? height.hashCode() : 0);
        result = 31 * result + (preserveProportion ? 1 : 0);
        return result;
    }
}
