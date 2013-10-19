package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

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
    public void setCurrentState(WidgetState currentState) {
        setTrimmedText((HasText) impl, ((LabelState) currentState).getLabel());
    }

    @Override
    public WidgetState getCurrentState() {
        LabelState data = new LabelState();
        data.setLabel(getTrimmedText((HasText) impl));
        return data;
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
