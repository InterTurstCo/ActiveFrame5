package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.DecoratedDateTimeBox;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.util.Date;

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
            DecoratedDateTimeBox dateBoxDecorate = (DecoratedDateTimeBox) impl;
            dateBoxDecorate.setValue(dbState);
        } else {
            if (dbState.getDateTimeContext().getDateTime() != null) {
                final Date date = DateTimeFormat.getFormat(ModelUtil.DTO_PATTERN)
                        .parse(dbState.getDateTimeContext().getDateTime());
                final DateTimeFormat formatter = DateTimeFormat.getFormat(dbState.getPattern());
                final StringBuilder textBuilder = new StringBuilder(formatter.format(date));
                if (dbState.isDisplayTimeZoneChoice()
                        && dbState.getDateTimeContext().getOrdinalFieldType() == FieldType.DATETIMEWITHTIMEZONE
                        .ordinal()) {
                    textBuilder.append(" ").append(dbState.getDateTimeContext().getTimeZoneId());
                }
                ((HasText) impl).setText(textBuilder.toString());
            }
        }
    }

    @Override
    protected boolean isChanged() {
        final DateBoxState state = getInitialData();
        final String initValue = state.getDateTimeContext() == null ? null : state.getDateTimeContext().getDateTime();
        final DecoratedDateTimeBox decorate = (DecoratedDateTimeBox) impl;
        final String currentValue = decorate.getText();
        return currentValue == null ? initValue != null : !currentValue.equals(initValue);
    }

    @Override
    protected WidgetState createNewState() {
        final DateBoxState initial = getInitialData();
        if (isEditable) {
            final DecoratedDateTimeBox decorate = (DecoratedDateTimeBox) impl;
            initial.getDateTimeContext().setDateTime(decorate.getText());
            final String selectedTimezoneId = decorate.getSelectedTimeZoneId();
            if (selectedTimezoneId != null) {
                initial.getDateTimeContext().setTimeZoneId(selectedTimezoneId);
            }
        }
        DateBoxState data = new DateBoxState();
        data.setDateTimeContext(initial.getDateTimeContext());
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        DecoratedDateTimeBox dateBoxDecorate = new DecoratedDateTimeBox(this);
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
        Date value = ((DecoratedDateTimeBox) impl).getValue();
        return value != null ? value.getTime() + "" : null;
    }


    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        ((DecoratedDateTimeBox) impl).getDateBox().addStyleName("validation-error");
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        ((DecoratedDateTimeBox) impl).getDateBox().removeStyleName("validation-error");
    }
}
