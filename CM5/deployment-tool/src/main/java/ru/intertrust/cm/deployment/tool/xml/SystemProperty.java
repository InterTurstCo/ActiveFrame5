package ru.intertrust.cm.deployment.tool.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by Alexander Bogatyrenko on 09.08.16.
 * <p>
 * This class represents...
 */
@Root(name = "property")
public class SystemProperty {

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
