package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 26.10.13
 *         Time: 22:25
 */
public abstract class ValueEditingWidgetState extends WidgetState {
    public abstract Value getValue();
}
