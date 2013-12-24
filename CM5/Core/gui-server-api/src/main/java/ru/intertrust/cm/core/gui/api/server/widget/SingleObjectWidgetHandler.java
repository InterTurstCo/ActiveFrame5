package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 14:58
 */
public abstract class SingleObjectWidgetHandler extends WidgetHandler {
    public abstract Value getValue(ValueEditingWidgetState state);
}
