package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 */
@Root(name = "radio-button")
public class RadioButtonConfig extends SingleSelectionWidgetConfig implements Dto {

    @Element(name = "layout", required = false)
    LayoutConfig layoutConfig;

    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(LayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    @Override

    public String getComponentName() {
        return "radio-button";
    }

    @Override
    public String getLogicalValidatorComponentName() {
        return "radioButtonLogicalValidator";
    }
}
