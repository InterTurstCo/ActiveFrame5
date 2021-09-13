package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeEndConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeStartConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.FormRangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.FormRangeDateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.DecoratedDateTimeBox;
import ru.intertrust.cm.core.gui.impl.client.localization.PlatformDateTimeFormat;
import ru.intertrust.cm.core.gui.impl.client.validation.DateRangeValidator;
import ru.intertrust.cm.core.gui.impl.client.validation.DateValidator;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.model.validation.Validator;

import java.util.Date;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:25
 */
@ComponentName("date-box")
public class DateBoxWidget extends BaseWidget implements FormRangeDateSelectedEventHandler {
    private DateBoxState dbState;
    private HandlerRegistration handlerRegistration;

    @Override
    public Component createNew() {
        return new DateBoxWidget();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    public void setCurrentState(WidgetState currentState) {
        dbState = (DateBoxState) currentState;
        if (isEditable) {
            DecoratedDateTimeBox dateBoxDecorate = (DecoratedDateTimeBox) impl;
            dateBoxDecorate.setValue(dbState);
        } else {
            if (dbState.getDateTimeContext().getDateTime() != null) {
                final Date date = PlatformDateTimeFormat.getFormat(ModelUtil.DTO_PATTERN)
                        .parse(dbState.getDateTimeContext().getDateTime());
                final DateTimeFormat formatter = PlatformDateTimeFormat.getDateTimeFormat(dbState.getPattern());
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
        String initValue = null;
        if (state.getDateTimeContext() != null) {
            String dateTime = state.getDateTimeContext().getDateTime();
            if (dateTime != null) {
                final DateTimeFormat dtoDateTimeFormat = PlatformDateTimeFormat.getFormat(ModelUtil.DTO_PATTERN);
                final Date initDate = dtoDateTimeFormat.parse(state.getDateTimeContext().getDateTime());
                final DateTimeFormat stateDateTimeFormat = PlatformDateTimeFormat.getFormat(state.getPattern());
                final Date clientDate = stateDateTimeFormat.parse(stateDateTimeFormat.format(initDate));
                initValue = dtoDateTimeFormat.format(clientDate);
            }
        }
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
        handlerRegistration = eventBus.addHandler(FormRangeDateSelectedEvent.TYPE, this);
        onDetach(dateBoxDecorate);
        return dateBoxDecorate;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        Label noneEditableWidget = new Label();
        noneEditableWidget.setStyleName("not-edit-date-text-label");
        onDetach(noneEditableWidget);
        return noneEditableWidget;
    }

    @Override
    protected void onDetach(Widget widget) {
        widget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (!attachEvent.isAttached()) {
                    clearHandlers();
                }
            }
        });
    }

    @Override
    protected void clearHandlers() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
    }

    @Override
    public Object getValue() {
        Object value = ((DecoratedDateTimeBox) impl).getValue();
        return value != null ? value : ""; //return "" rather than null to be able to validate for non-emptiness
    }

    @Override
    public List<Validator> getValidators() {
        List<Validator> validators = super.getValidators();

        for (Constraint constraint : getInitialData().getConstraints()) {
            if (constraint.getType() == Constraint.Type.DATE) {
                constraint.addParam(Constraint.PARAM_DATE_PATTERN, dbState.getPattern());
                validators.add(new DateValidator(constraint));
            }
            if (constraint.getType() == Constraint.Type.DATE_RANGE) {
                constraint.addParam(Constraint.PARAM_DATE_PATTERN, dbState.getPattern());
                validators.add(new DateRangeValidator(constraint));
            }
        }
        return validators;
    }

    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        ((DecoratedDateTimeBox) impl).getDateBox().addStyleName("validation-error");
        impl.addStyleName("validation-error");
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        if (isEditable()) {
            ((DecoratedDateTimeBox) impl).getDateBox().removeStyleName("validation-error");
        }
        impl.removeStyleName("validation-error");
    }

    @Override
    public void onFormRangeDateSelected(FormRangeDateSelectedEvent event) {
        DecoratedDateTimeBox dateBoxDecorate = (DecoratedDateTimeBox) impl;
        handleIfCurrentWidgetIsStartOfRange(dateBoxDecorate, event);
        handleIfCurrentWidgetIsEndOfRange(dateBoxDecorate, event);
    }

    private void handleIfCurrentWidgetIsEndOfRange(DecoratedDateTimeBox dateBoxDecorate, FormRangeDateSelectedEvent event) {

        RangeStartConfig rangeStartConfigFromState = dbState.getDateBoxConfig().getRangeStartConfig();
        if (rangeStartConfigFromState != null) {
            if (rangeStartConfigFromState.equals(event.getRangeStartConfig())) {
                dateBoxDecorate.setValue(event.getEndDate());
            }

            RangeEndConfig rangeEndConfigFromEvent = event.getRangeEndConfig();
            if (rangeEndConfigFromEvent == null) {
                return;
            }
            String widgetId = dbState.getDateBoxConfig().getId();
            if (widgetId.equalsIgnoreCase(rangeEndConfigFromEvent.getWidgetId())) {
                dateBoxDecorate.setValue(event.getEndDate());
            }
        }
    }

    private void handleIfCurrentWidgetIsStartOfRange(DecoratedDateTimeBox dateBoxDecorate, FormRangeDateSelectedEvent event) {
        RangeEndConfig rangeEndConfigFromState = dbState.getDateBoxConfig().getRangeEndConfig();
        if (rangeEndConfigFromState != null) {
            if (rangeEndConfigFromState.equals(event.getRangeEndConfig())) {
                dateBoxDecorate.setValue(event.getStartDate());
            }

            RangeStartConfig rangeStartConfigFromEvent = event.getRangeStartConfig();
            if (rangeStartConfigFromEvent == null) {
                return;
            }
            String widgetId = dbState.getDateBoxConfig().getId();
            if (widgetId.equalsIgnoreCase(rangeStartConfigFromEvent.getWidgetId())) {
                dateBoxDecorate.setValue(event.getStartDate());
            }
        }
    }
}
