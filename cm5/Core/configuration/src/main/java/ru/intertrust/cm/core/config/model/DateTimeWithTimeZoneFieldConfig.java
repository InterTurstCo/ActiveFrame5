package ru.intertrust.cm.core.config.model;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:13 AM
 */
public class DateTimeWithTimeZoneFieldConfig extends FieldConfig {

    @Override
    public FieldType getFieldType() {
        return FieldType.DATETIMEWITHTIMEZONE;
    }
}
