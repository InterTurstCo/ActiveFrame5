package ru.intertrust.cm.core.config.model.gui.form;

/**
 * @author Denis Mitavskiy
 *         Date: 10.09.13
 *         Time: 19:04
 */
public class TabGroupConfig {
    private String name;
    private LayoutConfig layout;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LayoutConfig getLayout() {
        return layout;
    }

    public void setLayout(LayoutConfig layout) {
        this.layout = layout;
    }
}
