package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.09.13
 * Time: 16:21
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "collection")
public class CollectionNestedConfig implements Serializable {
    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "use-default", required = false)
    private boolean useDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionNestedConfig that = (CollectionNestedConfig) o;

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        if (useDefault != that.isUseDefault()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return result * 23;
    }
}


