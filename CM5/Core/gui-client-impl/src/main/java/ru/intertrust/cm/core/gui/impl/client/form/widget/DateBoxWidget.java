package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:25
 */
@ComponentName("date-box")
public class DateBoxWidget extends BaseWidget {

    @Override
    public Component createNew() {
        return new DateBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        final DateBoxState dbState = (DateBoxState)currentState;
        dbState.getDateTimeContext().setChanged(false);
        if (isEditable) {
            DateBoxDecorate dateBox = (DateBoxDecorate) impl;
            dateBox.setValue(dbState.getDateTimeContext());
        } else {
            setTrimmedText((HasText) impl, dbState.getDateTimeContext().getDateTime());
        }
    }

    @Override
    public WidgetState getCurrentState() {
        final DateBoxState initial = getInitialData();
        if (isEditable) {
            initial.getDateTimeContext().setDateTime(((DateBoxDecorate) impl).getText());
        }
        DateBoxState data = new DateBoxState();
        data.setDateTimeContext(initial.getDateTimeContext());
        validate();
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        DateBoxDecorate dateBoxDecorate = new DateBoxDecorate();
        return dateBoxDecorate;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        Label noneEditableWidget = new Label();
        noneEditableWidget.removeStyleName("gwt-Label");
        return noneEditableWidget;
    }

    /**
     * todo will be used {@link DateBoxDecorate#getText()} method.
     * @return
     */
    @Override
    public Object getValue() {
        return ((DateBoxDecorate) impl).getValue().toString();//TODO: [validation] get raw string value
    }
}
