package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.RangeDatePickerPopup;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.TimeUtil;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
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

    public RangeDateHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                 List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, VALUE_SEPARATOR);

    }

    public void init() {
        initHtml();
        EventBus eventBus = new SimpleEventBus();
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
                    endDate = endDateString == null || endDateString.isEmpty() ? null
                            : dateTimeFormat.parse(endDateString);


                }
            } catch (IllegalArgumentException ex) {
                Window.alert("Неверный формат времени! Попробуйте " + dateTimeFormat.getPattern());
            }
            popupDatePicker = new RangeDatePickerPopup(startDate, endDate, eventBus, showTime, showSeconds);
        }

        initHandlers(eventBus);

    }

    private void initHandlers(EventBus eventBus) {

        eventBus.addHandler(RangeDateSelectedEvent.TYPE, new RangeDateSelectedEventHandler() {
            @Override
            public void onRangeDateSelected(RangeDateSelectedEvent event) {
                Date startDate = event.getStartDate();
                String startDateValue = dateTimeFormat.format(startDate);
                Date endDate = event.getEndDate();
                String endDateValue = dateTimeFormat.format(endDate);
                StringBuilder filterValueBuilder = new StringBuilder(startDateValue);
                filterValueBuilder.append(VALUE_SEPARATOR);
                filterValueBuilder.append(endDateValue);
                String filterValueRepresentation = filterValueBuilder.toString();
                setFilterValuesRepresentation(filterValueRepresentation);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(filterValueRepresentation);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART).setClassName("search-box-clear-button-on");
            }

        });

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(VALUE_SEPARATOR, filterValuesRepresentation);
    }
}