package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:20
 */
public interface LiteralFieldValueParser extends ComponentHandler {
    Value textToValue(FieldValueConfig fieldValueConfig, FieldConfig fieldConfig);

    Value textToValue(String valueText, String fieldTypeStr);

    Value textToValue(String valueText, FieldType fieldType);

    Value getCurrentTimeValue(String fieldTypeStr, String timeZoneId, boolean setCurrentMoment);
}
