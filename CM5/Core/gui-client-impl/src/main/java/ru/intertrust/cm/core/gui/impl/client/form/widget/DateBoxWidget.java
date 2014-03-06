package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.Event;
import java.util.Date;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.util.StringUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

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
        final DateBoxState dbState = (DateBoxState) currentState;
        if (isEditable) {
            DateBoxDecorate dateBox = (DateBoxDecorate) impl;
            dateBox.setValue(dbState);
        } else {
            final Date date = DateTimeFormat.getFormat(DateTimeContext.DTO_PATTERN)
                    .parse(dbState.getDateTimeContext().getDateTime());
            final DateTimeFormat formatter = DateTimeFormat.getFormat(dbState.getPattern());
            final StringBuilder textBuilder = new StringBuilder(formatter.format(date));
            if (dbState.isDisplayTimeZoneChoice()
                    && dbState.getDateTimeContext().getOrdinalFieldType() == FieldType.DATETIMEWITHTIMEZONE.ordinal()) {
                textBuilder.append(" ").append(dbState.getDateTimeContext().getTimeZoneId());
            }
            ((HasText) impl).setText(textBuilder.toString());
        }
    }

    @Override
    public WidgetState getCurrentState() {
        final DateBoxState initial = getInitialData();
        if (isEditable) {
            final DateBoxDecorate decorate = (DateBoxDecorate) impl;
            initial.getDateTimeContext().setDateTime(decorate.getText());
            final String selectedTimezoneId = decorate.getSelectedTimeZoneId();
            if (selectedTimezoneId != null) {
                initial.getDateTimeContext().setTimeZoneId(selectedTimezoneId);
            }
        }
        DateBoxState data = new DateBoxState();
        data.setDateTimeContext(initial.getDateTimeContext());
        validate();
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        DateBoxDecorate dateBoxDecorate = new DateBoxDecorate();
        DateBox dateBox = dateBoxDecorate.getDateBox();
        Event.sinkEvents(dateBox.getElement(), Event.ONBLUR);
        dateBox.addHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
            }
        }, BlurEvent.getType());

        return dateBoxDecorate;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        Label noneEditableWidget = new Label();
        noneEditableWidget.setStyleName("not-edit-date-text-label");
        return noneEditableWidget;
    }

    @Override
    public Object getValue() {
        Date value = ((DateBoxDecorate) impl).getValue();
        return value != null ? value.getTime() + "" : null;
    }


    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        ((DateBoxDecorate)impl).getDateBox().addStyleName("validation-error");
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        ((DateBoxDecorate)impl).getDateBox().removeStyleName("validation-error");
    }
}
