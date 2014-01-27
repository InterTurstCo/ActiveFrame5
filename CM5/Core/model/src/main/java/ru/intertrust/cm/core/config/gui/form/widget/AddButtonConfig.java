package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 23.01.14
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "add-button")
public class AddButtonConfig implements Dto{

    @Attribute(name = "image")
    String image;

    @Attribute(name = "text")
    String text;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClearAllButtonConfig that = (ClearAllButtonConfig) o;

        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}

