package ru.intertrust.cm.core.config.gui.navigation.counters;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 03.08.2014
 *         Time: 12:34
 */
@Root(name = "object-read-handler")
public class ObjectReadHandlerConfig implements Dto {
    @Attribute(name = "component-name")
    private String componentName;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectReadHandlerConfig that = (ObjectReadHandlerConfig) o;

        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return componentName != null ? componentName.hashCode() : 0;
    }
}
