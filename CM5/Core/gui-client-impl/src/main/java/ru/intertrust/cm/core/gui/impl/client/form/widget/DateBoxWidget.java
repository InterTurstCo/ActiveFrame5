package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:25
 */
@ComponentName("date-box")
public class DateBoxWidget extends BaseWidget {

    public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd/MM/yyyy hh:mm:ss");

    @Override
    public Component createNew() {
        return new DateBoxWidget();
    }

    public void setCurrentState(WidgetData state) {
        Date value = ((DateBoxData) state).getValue();
        if (isEditable) {
            DateBox dateBox = (DateBox) impl;
            dateBox.setValue(value);
        } else {
            setTrimmedText((HasText) impl, value == null ? "" : DATE_TIME_FORMAT.format(value));
        }
    }

    @Override
    public WidgetData getCurrentState() {
        DateBoxData data = new DateBoxData();
        if (isEditable) {
            data.setValue(((DateBox) impl).getValue());
        } else {
            data.setValue(DATE_TIME_FORMAT.parse(((Label) impl).getText()));
        }
        return data;
    }

    @Override
    protected Widget asEditableWidget() {
        return new DateBox();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}
