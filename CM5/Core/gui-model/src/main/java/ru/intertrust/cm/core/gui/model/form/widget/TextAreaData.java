package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:26
 */
public class TextAreaData extends TextBoxData {
    public TextAreaData() {
    }

    public TextAreaData(String text) {
        super(text);
    }

    @Override
    public String getComponentName() {
        return "text-area";
    }
}
