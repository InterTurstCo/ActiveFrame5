package ru.intertrust.cm.core.gui.model.history;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 14:51.
 */
public class HistoryItem {

    public static enum Type {URL, SESSION}

    private final Type type;
    private final String name;
    private final Object value;

    public HistoryItem(final Type type, final String name, final Object value) {
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

    public Object getValue() {
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
