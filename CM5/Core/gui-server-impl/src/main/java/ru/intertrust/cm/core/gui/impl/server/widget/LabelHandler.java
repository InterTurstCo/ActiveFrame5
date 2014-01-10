package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.text.SimpleDateFormat;
import java.util.Date;

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
            Object plainValue = context.getFieldPlainValue();
            if (plainValue == null) {
                return new LabelState("");
            }
            if (plainValue instanceof Date) {
                return new LabelState(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format((Date) plainValue));
            }

            return new LabelState(plainValue.toString());
        } else {
            LabelConfig widgetConfig = context.getWidgetConfig();
            return new LabelState(widgetConfig.getText());
        }
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
