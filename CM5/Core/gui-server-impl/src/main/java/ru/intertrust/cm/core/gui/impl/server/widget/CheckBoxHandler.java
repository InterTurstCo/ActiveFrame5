package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.CheckBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CheckBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
@ComponentName("check-box")
public class CheckBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public CheckBoxState getInitialState(WidgetContext context) {
        CheckBoxConfig cConfig = context.getWidgetConfig();
        CheckBoxState cState = new CheckBoxState(context.<Boolean>getFieldPlainValue());
        cState.setText(cConfig.getText());
        return cState;
    }

    @Override
    public Value getValue(WidgetState state) {
        final boolean booleanValue = ((CheckBoxState) state).isSelected();
        return new BooleanValue(booleanValue);
    }
}
