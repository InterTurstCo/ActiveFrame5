package ru.intertrust.cm.core.config.gui.form.widget.buttons;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.10.2014
 *         Time: 10:59
 */
public class ButtonConfig implements Dto {
    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "container-style-name", required = false)
    private String containerStyleName;

    @Attribute(name = "text-style-name", required = false)
    private String textStyleName;

    @Attribute(name = "text", required = false)
    @Localizable
    private String text;

    @Attribute(name = "display", required = false)
    private boolean display = true;


    public void setDisplay(boolean display) {
        this.display = display;
    }

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

    public String getContainerStyleName() {
        return containerStyleName;
    }

    public void setContainerStyleName(String styleName) {
        this.containerStyleName = styleName;
    }

    public String getTextStyleName() {
        return textStyleName;
    }

    public void setTextStyleName(String textStyleName) {
        this.textStyleName = textStyleName;
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
        if (containerStyleName != null ? !containerStyleName.equals(that.containerStyleName) : that.containerStyleName != null) {
            return false;
        }
        if (textStyleName != null ? !textStyleName.equals(that.textStyleName) : that.textStyleName != null) {
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
        result = 31 * result + (containerStyleName != null ? containerStyleName.hashCode() : 0);
        result = 31 * result + (textStyleName != null ? textStyleName.hashCode() : 0);
        result = 31 * result + (display ? 1 : 0);
        return result;
    }
}
