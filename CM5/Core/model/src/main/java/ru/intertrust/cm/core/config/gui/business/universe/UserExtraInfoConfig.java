package ru.intertrust.cm.core.config.gui.business.universe;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 15.04.2015
 *         Time: 15:48
 */
@Root(name = "user-extra-info")
public class UserExtraInfoConfig implements Dto {
    @Attribute(name = "component")
    private String component;

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}
