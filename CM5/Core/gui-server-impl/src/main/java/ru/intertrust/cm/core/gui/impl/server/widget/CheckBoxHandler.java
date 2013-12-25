package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CheckBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
@ComponentName("check-box")
public class CheckBoxHandler extends SingleObjectWidgetHandler {
    @Override
    public CheckBoxState getInitialState(WidgetContext context) {
        return new CheckBoxState(context.<Boolean>getFieldPlainValue());
    }

    @Override
    public Value getValue(ValueEditingWidgetState state) {
        final boolean booleanValue = ((CheckBoxState) state).isSelected();
        return new BooleanValue(booleanValue);
    }
}
