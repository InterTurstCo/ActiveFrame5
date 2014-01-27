package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:26
 */
@ComponentName("text-area")
public class TextAreaHandler extends ValueEditingWidgetHandler {
    @Override
    public TextState getInitialState(WidgetContext context) {
        return new TextState(context.<String>getFieldPlainValue());
    }

    @Override
    public Value getValue(WidgetState state) {
        return new StringValue(((TextState) state).getText());
    }
}
