package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 12:35.
 */
@Root(name = "action-param")
public class ActionParamConfig {

    @Attribute
    private String name;

    @Attribute
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
