package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 11:40
 */
@Root(name = "check-box")
public class CheckBoxConfig extends WidgetConfig {

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return  super.hashCode();
    }

    @Override
    public String getComponentName() {
        return "check-box";
    }

    @Override
    public String getLogicalValidatorComponentName() {
        return "checkBoxLogicalValidator";
    }
}
