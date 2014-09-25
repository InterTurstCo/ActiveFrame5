package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.CollectionDatePicker;
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
 *         Time: 22:54
 */
public class OneDateFilterHeaderWidget extends DateFilterHeaderWidget {

    public OneDateFilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                     List<String> initialFilterValues) {
        super(column, columnProperties, initialFilterValues, null);
    }

    public void init() {
        initHtml();
        boolean showTime = timePattern == null ? false : !TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType);
        Date date = null;
        try {
            date = filterValuesRepresentation == null || filterValuesRepresentation.isEmpty() ? null
                    : dateTimeFormat.parse(filterValuesRepresentation);
        } catch (IllegalArgumentException ex) {
            ApplicationWindow.errorAlert(BusinessUniverseConstants.WRONG_DATE_FORMAT_ERROR_MESSAGE
                    + dateTimeFormat.getPattern());
        }
        boolean showSeconds = TimeUtil.showSeconds(dateTimeFormat.getPattern());
        popupDatePicker = new CollectionDatePicker(date, eventBus, showTime, showSeconds);
        initHandlers();

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(null, filterValuesRepresentation);
    }

    protected void initHandlers() {

        eventBus.addHandler(DateSelectedEvent.TYPE, new DateSelectedEventHandler() {
            @Override
            public void onDateSelected(DateSelectedEvent event) {
                if(event.isDead() || !popupDatePicker.equals(event.getSource())){
                    return;
                }
                event.kill();
                Date date = event.getDate();
                String dateValue = dateTimeFormat.format(date);
                setFilterValuesRepresentation(dateValue);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(dateValue);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART)
                        .setClassName(GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn());
                setFocused(true);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();

            }

        });

    }

}

