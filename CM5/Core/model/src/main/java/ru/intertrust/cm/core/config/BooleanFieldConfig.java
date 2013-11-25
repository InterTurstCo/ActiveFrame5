package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:11 AM
 */
public class BooleanFieldConfig extends FieldConfig {

    @Override
    public FieldType getFieldType() {
        return FieldType.BOOLEAN;
    }
}
