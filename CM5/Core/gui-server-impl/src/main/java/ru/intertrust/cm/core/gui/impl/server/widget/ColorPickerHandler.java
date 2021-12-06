package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ColorPickerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by Myskin Sergey on 11.01.2021.
 */
@ComponentName("color-picker")
public class ColorPickerHandler extends ValueEditingWidgetHandler {

    @Override
    public ColorPickerState getInitialState(WidgetContext context) {
        return new ColorPickerState(context.<String>getFieldPlainValue());
    }

    @Override
    public Value getValue(WidgetState state) {
        return new StringValue(((ColorPickerState) state).getHexCode());
    }

}
