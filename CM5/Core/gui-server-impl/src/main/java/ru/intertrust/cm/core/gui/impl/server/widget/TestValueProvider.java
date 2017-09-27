package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.DefaultValueProvider;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by Ravil on 27.09.2017.
 */
@ComponentName("default.value.provider")
public class TestValueProvider implements DefaultValueProvider {
    @Override
    public String provide(FormState fState) {
        return "Test default value";
    }
}
