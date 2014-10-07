package ru.intertrust.cm.core.gui.impl.server.widget.custom;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.EnumerationMapProvider;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 07.10.14
 *         Time: 12:21
 */
@ComponentName("custom.enum.map.provider")
public class CustomEnumMapProvider implements EnumerationMapProvider, ComponentHandler {

    @Override
    public Map<String, Value> getMap(WidgetContext widgetContext) {

        Map<String, Value> result = new HashMap<>();
        result.put("value1", new StringValue("001"));
        result.put("value2", new StringValue("002"));
        result.put("value3", new StringValue(("003")));
        result.put("<empty>",new StringValue(null));
        return result;
    }
}
