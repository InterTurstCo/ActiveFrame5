package ru.intertrust.cm.core.gui.impl.client.converter;

import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.HashMap;

/**
 * @author Sergey.Okolot
 *         Created on 21.01.14 10:54.
 */
public class DefaultValueConverter<T extends Value> implements ValueConverter<T> {

    @Override
    public T stringToValue(String asString) {
        return null;
    }

    @Override
    public String valueToString(T value) {
        if (value == null || value.get() == null) {
            return "";
        } else {
            return value.get().toString();
        }
    }

    @Override
    public void init(HashMap<String, Object> params) {
    }
}
