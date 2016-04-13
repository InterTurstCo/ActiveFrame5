package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 01.04.2016
 * Time: 11:59
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "current-row-change")
public class CurrentRowChangeConfig implements Dto {
    @Attribute(name = "component", required = true)
    private String component;

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}
