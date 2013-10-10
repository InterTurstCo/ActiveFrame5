package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 13:49
 */
@Root(name = "text-area")
public class TextAreaConfig extends WidgetConfig implements Dto {
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
        return "text-area";
    }
}