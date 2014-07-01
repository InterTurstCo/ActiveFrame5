package ru.intertrust.cm.core.dao.impl.doel;

import java.util.Collections;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

@DoelFunction(name = "min",
        contextTypes = {
                FieldType.LONG, FieldType.DECIMAL,
                FieldType.DATETIME, FieldType.DATETIMEWITHTIMEZONE, FieldType.TIMELESSDATE },
        resultMultiple = false)
public class MinimumFunction implements DoelFunctionImplementation {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        T min = null;
        for (Object value : context) {
            if (min == null || min.compareTo(value) >= 0) {
                min = (T) value;
            }
        }
        return min == null ? Collections.<T>emptyList() : Collections.singletonList(min);
    }

}
