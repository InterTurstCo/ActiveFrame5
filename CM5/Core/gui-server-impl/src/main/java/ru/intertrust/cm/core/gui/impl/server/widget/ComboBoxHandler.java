package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.SingleSelectionWidgetState;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("combo-box")
public class ComboBoxHandler extends SingleSelectionWidgetHandler {

    @Override
    public SingleSelectionWidgetState getInitialState(WidgetContext context) {
        ComboBoxState result = new ComboBoxState();
        setupInitialState(result, context);

        Map<Id, String> idDisplayMapping = result.getListValues();
        idDisplayMapping.put(null, "");

        return result;
    }
}
