package ru.intertrust.cm.core.config.gui.navigation;

/**
 * Created by Ravil Abdulkhairov on 11.03.2016.
 */
public enum OpenPositionTypeConfig {
    CURRENT("current"),
    TAB("tab"),
    WINDOW("window");

    private final String value;

    OpenPositionTypeConfig(String v)
    {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OpenPositionTypeConfig fromValue(String v) {
        for (OpenPositionTypeConfig c: OpenPositionTypeConfig.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
