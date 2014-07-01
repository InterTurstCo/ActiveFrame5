package ru.intertrust.cm.core.dao.impl.doel;

import java.util.Collections;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

@DoelFunction(name = "max",
contextTypes = {
        FieldType.LONG, FieldType.DECIMAL,
        FieldType.DATETIME, FieldType.DATETIMEWITHTIMEZONE, FieldType.TIMELESSDATE },
resultMultiple = false)
public class MaximumFunction implements DoelFunctionImplementation {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        T max = null;
        for (Object value : context) {
            if (max == null || max.compareTo(value) <= 0) {
                max = (T) value;
            }
        }
        return max == null ? Collections.<T>emptyList() : Collections.singletonList(max);
    }

}
