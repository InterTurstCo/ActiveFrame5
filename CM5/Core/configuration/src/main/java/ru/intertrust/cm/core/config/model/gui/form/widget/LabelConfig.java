package ru.intertrust.cm.core.config.model.gui.form.widget;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "label")
public class LabelConfig extends WidgetConfig implements Dto {
    @Element(name = "text", required = false)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
