package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.OneDatePickerPopup;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.06.2014
 *         Time: 22:54
 */
public class OneDateFilterHeaderWidget extends DateFilterHeaderWidget {

    public OneDateFilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                     List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, null);
    }

    public void init() {
        initHtml();
        EventBus eventBus = new SimpleEventBus();
        boolean showTime = timePattern == null ? false : !TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType);
        try {
            Date date = filterValuesRepresentation == null || filterValuesRepresentation.isEmpty() ? null
                    : dateTimeFormat.parse(filterValuesRepresentation);
            popupDatePicker = new OneDatePickerPopup(date, eventBus, showTime);
        } catch (IllegalArgumentException ex) {
            Window.alert("Неверный формат времени! Попробуйте " + dateTimeFormat.getPattern());
        }
        initHandlers(eventBus);

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(null, filterValuesRepresentation);
    }

    protected void initHandlers(EventBus eventBus) {

        eventBus.addHandler(DateSelectedEvent.TYPE, new DateSelectedEventHandler() {
            @Override
            public void onDateSelected(DateSelectedEvent event) {
                Date date = event.getDate();
                String dateValue = dateTimeFormat.format(date);
                setFilterValuesRepresentation(dateValue);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(dateValue);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART).setClassName("search-box-clear-button-on");
            }

        });

    }

}

