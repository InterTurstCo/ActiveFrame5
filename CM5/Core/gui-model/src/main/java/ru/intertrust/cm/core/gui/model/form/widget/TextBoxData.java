package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 11:42
 */
public class TextBoxData extends WidgetData {
    private String text;

    public TextBoxData() {
    }

    public TextBoxData(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getComponentName() {
        return "text-box";
    }

    @Override
    public Value toValue() {
        return new StringValue(text);
    }
}
