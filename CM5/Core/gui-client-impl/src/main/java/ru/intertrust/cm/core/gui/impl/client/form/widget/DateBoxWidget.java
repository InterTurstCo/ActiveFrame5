package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.util.StringUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.util.Date;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DATE_TIME_FORMAT;

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
        DateBoxState dateBoxState = (DateBoxState)currentState;
        Date value = dateBoxState.getDate();
       // RangeDateConfig rangeDateConfig = dateBoxState.getRangeDateValueConfig();
        if (isEditable) {
            DateBoxDecorate dateBox = (DateBoxDecorate) impl;
            dateBox.setValue(value);
        } else {
            setTrimmedText((HasText) impl, value == null ? "" : DATE_TIME_FORMAT.format(value));
        }
    }

    @Override
    public WidgetState getCurrentState() {
        DateBoxState data = new DateBoxState();
        if (isEditable) {
            data.setDate(((DateBoxDecorate) impl).getValue());
        } else {
            //data.setDate(DATE_TIME_FORMAT.parse(((DateBoxDecorate) impl).getTextField().getText()));
            data.setDate(DATE_TIME_FORMAT.parse(((DateBoxDecorate) impl).getValue().toString()));
        }
        validate();
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        DateBoxDecorate dateBoxDecorate = new DateBoxDecorate();
        return dateBoxDecorate;
        //return new DateBox();
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        Label noneEditableWidget = new Label();
        noneEditableWidget.removeStyleName("gwt-Label");
        return noneEditableWidget;
    }

    @Override
    public Object getValue() {
        return ((DateBoxDecorate) impl).getValue().toString();//TODO: [validation] get raw string value
    }

    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        impl.addStyleName("validation-error");
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        impl.removeStyleName("validation-error");
    }
}
