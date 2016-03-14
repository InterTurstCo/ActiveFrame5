package ru.intertrust.cm.core.config.gui.navigation;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 11.03.2016
 * Time: 17:27
 * To change this template use File | Settings | File and Code Templates.
 */
public enum BaseUrlPropertyTypeConfig {
    BASEURLONE("base.url.1"),
    BASEURLTWO("base.url.2");

    private final String value;

    BaseUrlPropertyTypeConfig(String v)
    {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BaseUrlPropertyTypeConfig fromValue(String v) {
        for (BaseUrlPropertyTypeConfig c: BaseUrlPropertyTypeConfig.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
