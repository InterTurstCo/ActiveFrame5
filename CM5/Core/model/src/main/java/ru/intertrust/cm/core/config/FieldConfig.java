package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 10:55 AM
 */
public abstract class FieldConfig {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "not-null", required = false)
    private boolean notNull;

    protected FieldConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }
}
