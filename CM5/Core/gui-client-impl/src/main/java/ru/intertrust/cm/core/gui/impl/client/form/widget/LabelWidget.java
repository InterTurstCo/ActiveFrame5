package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LabelData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 21:15
 */
@ComponentName("label")
public class LabelWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new LabelWidget();
    }

    @Override
    public void setCurrentState(WidgetData state) {
        setTrimmedText((HasText) impl, ((LabelData) state).getLabel());
    }

    @Override
    public WidgetData getCurrentState() {
        return null;
    }

    @Override
    protected Widget asEditableWidget() {
        return new Label();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }
}
