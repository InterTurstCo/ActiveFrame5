package ru.intertrust.cm.core.dao.impl.sqlparser;

import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

/**
 * 
 * @author atsvetkov
 *
 */
public class ReferenceFilterUtility {

    public static ReferenceValue getReferenceValue(Value value) {
        ReferenceValue refValue = null;
        if (value instanceof ReferenceValue) {
            refValue = (ReferenceValue) value;

        } else if (value instanceof StringValue) {
            // ссылочные параметры могут передаваться в строковом виде.
            String strParamValue = ((StringValue) value).get();
            refValue = new ReferenceValue(new RdbmsId(strParamValue));
        }
        return refValue;
    }
}
