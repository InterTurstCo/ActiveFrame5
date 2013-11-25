package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 23.10.13
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */

@Root(name = "selection-pattern")
public class SelectionPatternConfig implements Serializable {
    @Attribute(name = "value")
    String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
