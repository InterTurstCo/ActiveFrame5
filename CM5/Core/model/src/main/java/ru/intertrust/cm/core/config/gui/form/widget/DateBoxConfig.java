package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:17
 */
@Root(name = "date-box")
public class DateBoxConfig extends WidgetConfig implements Dto {
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
        return "date-box";
    }
}
