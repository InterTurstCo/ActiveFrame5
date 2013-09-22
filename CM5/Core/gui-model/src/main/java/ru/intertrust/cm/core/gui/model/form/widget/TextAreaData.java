package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:26
 */
public class TextAreaData extends WidgetData {
    private String text;

    public TextAreaData() {
    }

    public TextAreaData(String text) {
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
        return "text-area";
    }
}
