package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CheckBoxState;

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

}
