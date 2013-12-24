package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends SingleObjectWidgetHandler {
    @Override
    public LabelState getInitialState(WidgetContext context) {
        FieldPath fieldPath = context.getFieldPaths()[0];
        if (fieldPath != null) {
            String plainValue = context.getFieldPlainValue();
            return new LabelState(plainValue == null || plainValue.isEmpty() ? "" : plainValue);
        } else {
            LabelConfig widgetConfig = context.getWidgetConfig();
            return new LabelState(widgetConfig.getText());
        }
    }

    @Override
    public Value getValue(ValueEditingWidgetState state) {
        return null;
    }
}
