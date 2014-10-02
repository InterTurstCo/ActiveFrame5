package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 01.10.14.
 */
public class HandlerUtils {
    public static List<Id> takeDefaultReferenceValues(WidgetContext context) {
        List<Id> defaultIds = new ArrayList<>();
        for (Value value : context.getDefaultValues()) {
            if (value instanceof ReferenceValue) {
                defaultIds.add(((ReferenceValue) value).get());
            }
        }
        return defaultIds;
    }
}
