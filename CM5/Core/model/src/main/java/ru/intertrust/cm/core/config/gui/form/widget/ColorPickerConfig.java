package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by Myskin Sergey on 11.01.2021.
 */
@Root(name = "color-picker")
public class ColorPickerConfig extends WidgetConfig implements Dto {

    @Override
    public String getComponentName() {
        return "color-picker";
    }

}
