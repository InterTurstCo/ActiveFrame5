package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextAreaState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:26
 */
@ComponentName("text-area")
public class TextAreaHandler extends SingleObjectWidgetHandler {
    @Override
    public TextAreaState getInitialState(WidgetContext context) {
        return new TextAreaState(context.<String>getFieldPlainValue());
    }

    @Override
    public Value getValue(ValueEditingWidgetState state) {
        return new StringValue(((TextAreaState) state).getText());
    }
}
