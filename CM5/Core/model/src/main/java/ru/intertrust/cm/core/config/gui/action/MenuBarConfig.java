package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Sergey.Okolot
 *         Created on 11.04.2014 16:47.
 */
@Root(name = "menu-bar")
public class MenuBarConfig extends BaseAttributeConfig implements TopLevelConfig {

    @Attribute
    private String componentName;

    public String getComponentName() {
        return componentName;
    }

    @Override
    public String getName() {
        // todo will be implements
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MenuBarConfig that = (MenuBarConfig) o;

        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null)
            return false;

        return true;
    }
}
