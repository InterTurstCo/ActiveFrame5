package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 11:42
 */
public class TextState extends ValueEditingWidgetState {
    private String text;
    private boolean isEncrypted;

    public TextState() {
    }

    public TextState(String text, boolean isEncrypted) {
        this.text = text;
        this.isEncrypted = isEncrypted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
