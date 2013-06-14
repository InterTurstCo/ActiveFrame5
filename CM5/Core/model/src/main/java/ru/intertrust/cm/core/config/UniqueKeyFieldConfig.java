package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:58 AM
 */
public class UniqueKeyFieldConfig implements Serializable {

    @Attribute(name = "name")
    private String name;

    public UniqueKeyFieldConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
