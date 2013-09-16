package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "rich-text-area")
public class TextBoxConfig extends WidgetConfig implements Dto {
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return  super.hashCode();
    }
}
