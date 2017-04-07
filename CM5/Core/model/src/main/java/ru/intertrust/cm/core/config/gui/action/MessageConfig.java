package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.config.base.Localizable;

import java.io.Serializable;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:26.
 */
public class MessageConfig implements Serializable {

    @Attribute
    @Localizable
    private String text;

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

        MessageConfig that = (MessageConfig) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
