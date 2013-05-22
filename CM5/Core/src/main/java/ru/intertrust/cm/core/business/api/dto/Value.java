package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
 * Значение поля бизнес-объекта. По сути является отображением типов, определённых в системе на типы Java.
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:10
 */
public abstract class Value implements Serializable {
    /**
     * Создаёт значение поля бизнес-объекта
     */
    public Value() {
    }

    /**
     * Возвращает значение поля бизнес-объекта, отображённое в Java-тип
     * @return значение поля бизнес-объекта, отображённое в Java-тип
     */
    public abstract Object get();

    /**
     * Проверяет значение на "пустоту"
     * @return true, если объект "пуст" и false - в противном случае
     */
    public boolean isEmpty() {
        return get() == null;
    }

    @Override
    public int hashCode() {
        Object object = get();
        return object == null ? -1 : object.hashCode();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof Value)) {
            return false;
        }
        Object thisValue = get();
        Object anotherValue = ((Value) another).get();
        return thisValue == null && anotherValue == null || thisValue != null && thisValue.equals(anotherValue);
    }

    @Override
    public String toString() {
        Object value = get();
        return value == null ? "null" : value.toString();
    }
}
