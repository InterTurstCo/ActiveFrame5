package ru.intertrust.cm.core.gui.model.plugin;

/**
 * Created by Ravil on 25.01.2018.
 */
public enum ServerState {

    NORMAL("Нормальное"),
    PROBLEM("Проблемы"),
    FAULT("Отказ"),
    DECOMISSIONED("Выведен");

    private final String value;

    ServerState(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

}
