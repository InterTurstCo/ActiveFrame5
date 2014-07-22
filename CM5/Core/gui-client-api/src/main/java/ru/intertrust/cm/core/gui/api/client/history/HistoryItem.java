package ru.intertrust.cm.core.gui.api.client.history;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 14:51.
 */
public class HistoryItem {

    public static enum Type {URL, USER_INTERFACE, PLUGIN_CONDITION}

    private final Type type;
    private final String name;
    private final String value;

    public HistoryItem(final Type type, final String name, final String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringBuilder(HistoryItem.class.getSimpleName())
                .append(": type=").append(type)
                .append(", name=").append(name)
                .append(", value=").append(value)
                .toString();
    }
}
