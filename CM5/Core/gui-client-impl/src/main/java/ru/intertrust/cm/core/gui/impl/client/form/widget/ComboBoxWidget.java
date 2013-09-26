package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("combo-box")
public class ComboBoxWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new ComboBoxWidget();
    }

    public void setCurrentState(WidgetData state) {

    }

    @Override
    public WidgetData getCurrentState() {
        return new ComboBoxData();
    }

    @Override
    protected Widget asEditableWidget() {
        return new ListBox(false);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}
