package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.CollectionRangeDatePicker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.TimeUtil;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.06.2014
 *         Time: 0:00
 */
public class RangeDateHeaderWidget extends DateFilterHeaderWidget {
    private static final String VALUE_SEPARATOR = " - ";
    private String currentSeparator;

    public RangeDateHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                 List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, VALUE_SEPARATOR);

    }

    public void init() {
        initHtml();
        boolean showTime = timePattern == null ? false : !TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType);
        Date startDate = null;
        Date endDate = null;
        List<String> dateStrings = HeaderWidgetUtil.getFilterValues(VALUE_SEPARATOR, filterValuesRepresentation);
        boolean showSeconds = TimeUtil.showSeconds(dateTimeFormat.getPattern());
        if (!dateStrings.isEmpty()) {
            String startDateString = dateStrings.get(0);
            try {
                startDate = startDateString == null || startDateString.isEmpty() ? null
                        : dateTimeFormat.parse(startDateString);
                if (dateStrings.size() == 2) {
                    String endDateString = dateStrings.get(1);
                    if (startDateString.equalsIgnoreCase(endDateString)) {
                        endDate = (Date) startDate.clone();
                        currentSeparator = null;
                    } else {
                        endDate = endDateString == null || endDateString.isEmpty() ? null
                                : dateTimeFormat.parse(endDateString);
                        currentSeparator = VALUE_SEPARATOR;
                    }

                }
            } catch (IllegalArgumentException ex) {
                ApplicationWindow.errorAlert(BusinessUniverseConstants.WRONG_DATE_FORMAT_ERROR_MESSAGE
                        + dateTimeFormat.getPattern());
            }
            popupDatePicker = new CollectionRangeDatePicker(startDate, endDate, eventBus, showTime, showSeconds);
        }

        initHandlers();

    }

    private void initHandlers() {
        removePreviousHandlerIfExist();
        handlerRegistration = eventBus.addHandler(RangeDateSelectedEvent.TYPE, new RangeDateSelectedEventHandler() {
            @Override
            public void onRangeDateSelected(RangeDateSelectedEvent event) {
                if (!popupDatePicker.equals(event.getSource())) {
                    return;
                }
                Date startDate = event.getStartDate();
                String startDateValue = dateTimeFormat.format(startDate);
                Date endDate = event.getEndDate();
                String endDateValue = dateTimeFormat.format(endDate);
                StringBuilder filterValueBuilder = new StringBuilder(startDateValue);
                if (startDateValue.equalsIgnoreCase(endDateValue)) {

                    currentSeparator = null;
                } else {
                    filterValueBuilder.append(VALUE_SEPARATOR);
                    filterValueBuilder.append(endDateValue);
                    currentSeparator = VALUE_SEPARATOR;
                }
                String filterValueRepresentation = filterValueBuilder.toString();
                setFilterValuesRepresentation(filterValueRepresentation);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(filterValueRepresentation);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART)
                        .setClassName(GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn());
                setFocused(true);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();

            }

        });

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(currentSeparator, filterValuesRepresentation);
    }
}
