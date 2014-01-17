package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "combo-box")
public class ComboBoxConfig extends SingleSelectionWidgetConfig implements Dto {

    @Override
    public String getComponentName() {
        return "combo-box";
    }
}
