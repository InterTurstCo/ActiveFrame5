package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 23.10.13
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */

@Root(name = "selection-filter")
public class SelectionFilterConfig implements Serializable{

    @Attribute(name = "name")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
