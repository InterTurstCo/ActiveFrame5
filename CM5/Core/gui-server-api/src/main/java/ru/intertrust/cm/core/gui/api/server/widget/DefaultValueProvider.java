package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by Ravil on 27.09.2017.
 */
public interface DefaultValueProvider extends ComponentHandler {
    String provide(FormState fState);
}
