package ru.intertrust.cm.core.config.gui.form.widget.buttons;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.10.2014
 *         Time: 10:59
 */
public class ButtonConfig implements Dto {
    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "text", required = false)
    private String text;

    @Attribute(name = "display", required = false)
    private boolean display = true;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDisplay() {
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ButtonConfig  that = (ButtonConfig ) o;

        if (image != null ? !image.equals(that.image) : that.image != null) {
            return false;
        }
        if (text != null ? !text.equals(that.text) : that.text != null) {
            return false;
        }
        if (display != that.display) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (display ? 1 : 0);
        return result;
    }
}
