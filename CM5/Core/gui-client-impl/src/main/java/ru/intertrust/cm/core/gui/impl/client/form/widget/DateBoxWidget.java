package ru.intertrust.cm.core.gui.impl.client.form.widget;

import java.util.Date;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
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
