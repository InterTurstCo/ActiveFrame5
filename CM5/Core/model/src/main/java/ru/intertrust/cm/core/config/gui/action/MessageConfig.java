package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;
import org.simpleframework.xml.Attribute;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:26.
 */
public class MessageConfig implements Serializable {

    @Attribute
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
