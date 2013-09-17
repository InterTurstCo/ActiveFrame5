package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "integer-box")
public class IntegerBoxConfig extends WidgetConfig implements Dto {
    @Override
    public boolean equals(Object o) {
       return super.equals(o);
    }

    @Override
    public int hashCode() {
       return  super.hashCode();
    }
}
