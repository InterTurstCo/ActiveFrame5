package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:43
 */
public class SomePluginData extends PluginData {
    private String text;

    public SomePluginData() {
    }

    public SomePluginData(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SomePluginData {" +
                "text='" + text + '\'' +
                '}';
    }
}
