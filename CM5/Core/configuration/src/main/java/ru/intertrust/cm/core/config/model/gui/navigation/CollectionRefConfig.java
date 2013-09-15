package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 12/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-ref")
public class CollectionRefConfig implements Serializable {
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

        CollectionRefConfig that = (CollectionRefConfig) o;

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


