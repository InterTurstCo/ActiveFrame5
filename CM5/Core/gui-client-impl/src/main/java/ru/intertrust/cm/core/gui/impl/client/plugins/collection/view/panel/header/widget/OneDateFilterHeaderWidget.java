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
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_CLEAR_BUTTON_ID_PART;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_INPUT_ID_PART;

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
        Date date = null;
        try {
            date = filterValuesRepresentation == null || filterValuesRepresentation.isEmpty() ? null
                    : userDateTimeFormat.parse(filterValuesRepresentation);
        } catch (IllegalArgumentException ex) {
            ApplicationWindow.errorAlert("Ошибка в формате даты: " + getErrorMessage());
        }
        boolean showSeconds = TimeUtil.showSeconds(userDateTimeFormat.getPattern());
        popupDatePicker = new CollectionDatePicker(date, eventBus, isShowTime(), showSeconds);
        initHandlers();

    }

    @Override
    public List<String> getFilterValues() {
        return HeaderWidgetUtil.getFilterValues(null, convertUserDateStringToGuiDateString(filterValuesRepresentation));
    }

    protected void initHandlers() {
    removePreviousHandlerIfExist();
    handlerRegistration = eventBus.addHandler(DateSelectedEvent.TYPE, new DateSelectedEventHandler() {
            @Override
            public void onDateSelected(DateSelectedEvent event) {
                if (!popupDatePicker.equals(event.getSource())) {
                    return;
                }
                Date date = event.getDate();
                String userDateValue = userDateTimeFormat.format(date);
                setFilterValuesRepresentation(userDateValue);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(userDateValue);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART)
                        .setClassName(GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn());
                setFocused(true);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();

            }

        });

    }

}

