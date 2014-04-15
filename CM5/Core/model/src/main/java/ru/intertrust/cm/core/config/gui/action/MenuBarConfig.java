package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Sergey.Okolot
 *         Created on 11.04.2014 16:47.
 */
@Root(name = "menu-bar")
public class MenuBarConfig extends BaseAttributeConfig {

    @Attribute
    private String componentName;

    @Element(name = "menu-separator")
    private BaseAttributeConfig menuSeparatorConfig;



//    <xs:choice minOccurs="0" maxOccurs="unbounded">
//    <xs:element ref="act:abstract-action" />

    public String getComponentName() {
        return componentName;
    }

    public BaseAttributeConfig getMenuSeparatorConfig() {
        return menuSeparatorConfig;
    }
}
