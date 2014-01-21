package ru.intertrust.cm.core.gui.impl.client.converter;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.HashMap;

/**
 * @author Sergey.Okolot
 *         Created on 21.01.14 10:40.
 */
public interface ValueConverter<T extends Value> {

    T stringToValue(String asString);

    String valueToString(T value);

    void init(HashMap<String, Object> params);
}
