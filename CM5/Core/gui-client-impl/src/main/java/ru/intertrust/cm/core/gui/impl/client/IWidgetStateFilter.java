package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;

/**
 * Created by andrey on 14.03.14.
 */
public interface IWidgetStateFilter {
    boolean exclude(BaseWidget widget);
}
