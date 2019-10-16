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
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_CLEAR_BUTTON_ID_PART;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_INPUT_ID_PART;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.06.2014
 *         Time: 0:00
 */
public class RangeDateHeaderWidget extends DateFilterHeaderWidget {
    private static final String VALUE_SEPARATOR = " - ";

    public RangeDateHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                 List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, VALUE_SEPARATOR);

    }

    public void init() {
        initHtml();
        Date startDate = null;
        Date endDate = null;
        List<String> dateStrings = HeaderWidgetUtil.getFilterValues(VALUE_SEPARATOR, filterValuesRepresentation);
        boolean showSeconds = TimeUtil.showSeconds(userDateTimeFormat.getPattern());
        if (!dateStrings.isEmpty()) {
            String startDateString = dateStrings.get(0);
            try {
                startDate = startDateString == null || startDateString.isEmpty() ? null
                        : userDateTimeFormat.parse(startDateString);

                if (dateStrings.size() == 2) {
                    String endDateString = dateStrings.get(1);
                    if (startDate != null && startDateString != null && startDateString.equalsIgnoreCase(endDateString)) {
                        endDate = (Date) startDate.clone();
                    } else {
                        endDate = endDateString == null || endDateString.isEmpty() ? null
                                : userDateTimeFormat.parse(endDateString);

                    }

                }
            } catch (IllegalArgumentException ex) {
                ApplicationWindow.errorAlert("Ошибка в формате даты: " + getErrorMessage());
            }
            popupDatePicker = new CollectionRangeDatePicker(startDate, endDate, eventBus, isShowTime(), showSeconds);
        }

        initHandlers();

    }

    public List<String> getFilterValues() {

        List<String> userFilterValues = HeaderWidgetUtil.getFilterValues(VALUE_SEPARATOR, filterValuesRepresentation);
        List<String> guiFilterValues = new ArrayList<>(2);
        for (String userFilterValue : userFilterValues) {
            guiFilterValues.add(convertUserDateStringToGuiDateString(userFilterValue));
        }
        return guiFilterValues;
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
                String userStartDateValue = userDateTimeFormat.format(startDate);
                StringBuilder guiFilterValueBuilder = new StringBuilder();
                guiFilterValueBuilder.append(guiDateTimeFormat.format(startDate));
                Date endDate = event.getEndDate();
                String userEndDateValue = userDateTimeFormat.format(endDate);
                StringBuilder filterValueBuilder = new StringBuilder(userStartDateValue);
                if (!userStartDateValue.equalsIgnoreCase(userEndDateValue)) {
                    filterValueBuilder.append(VALUE_SEPARATOR);
                    filterValueBuilder.append(userEndDateValue);
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

}
