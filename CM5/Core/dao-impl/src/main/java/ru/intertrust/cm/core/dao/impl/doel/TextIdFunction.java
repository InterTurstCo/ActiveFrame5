package ru.intertrust.cm.core.dao.impl.doel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

@DoelFunction(name = "id",
        contextTypes = { FieldType.STRING }, changesType = true, resultType = FieldType.REFERENCE)
public class TextIdFunction implements DoelFunctionImplementation {

    private static final Logger logger = LoggerFactory.getLogger(TextIdFunction.class);

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        ArrayList<ReferenceValue> result = new ArrayList<>(context.size());
        for (Object value : context) {
            try {
                RdbmsId id = new RdbmsId(((StringValue) value).get());
                result.add(new ReferenceValue(id));
            } catch (Exception e) {
                logger.trace("Error parsing id " + ((Value) value).get(), e);
                continue;
            }
        }
        return (List<T>) result;
    }

}
