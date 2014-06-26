package ru.intertrust.cm.core.dao.impl.doel;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;

public interface DoelFunctionImplementation {

    @SuppressWarnings("rawtypes")
    <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken);
}
