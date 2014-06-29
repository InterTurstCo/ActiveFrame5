package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.i18n.client.DateTimeFormat;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.DatePickerPopup;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_CLEAR_BUTTON_ID_PART;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 21:29
 */
public abstract class DateFilterHeaderWidget extends FilterHeaderWidget {
    protected DatePickerPopup popupDatePicker;
    protected String fieldType;
    protected String timePattern;
    protected DateTimeFormat dateTimeFormat;

    public DateFilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                  List<String> initialFilterValues, String valueSeparator) {
        super(column, columnProperties, initialFilterValues, valueSeparator);
        fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        timePattern = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        dateTimeFormat = initDateTimeFormat(datePattern, timePattern);
    }

    protected void initHtml() {
        StringBuilder htmlBuilder = new StringBuilder(getTitleStyle());

        htmlBuilder.append(" <div class=\"search-container\"");
        htmlBuilder.append(getSearchContainerStyle());
        htmlBuilder.append("id = ");
        htmlBuilder.append(id);
        htmlBuilder.append("><input type=\"text\" class=\"gwt-DateBox search-data-box\" id= ");
        htmlBuilder.append(id);
        htmlBuilder.append("input ");
        htmlBuilder.append(getSearchInputStyle());
        htmlBuilder.append("<button type=\"button\" id= \"");
        htmlBuilder.append(id);
        htmlBuilder.append(HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART);

        htmlBuilder.append("\" class=\"date-select\" ></button><div type=\"button\" id= ");
        htmlBuilder.append(id);
        htmlBuilder.append(HEADER_CLEAR_BUTTON_ID_PART);
        String clearButtonClass = filterValuesRepresentation.isEmpty() ? " class=\"search-box-clear-button-off\"></div></div>"
                : " class=\"search-box-clear-button-on\"></div></div>";
        htmlBuilder.append(clearButtonClass);
        html = htmlBuilder.toString();
    }

    private DateTimeFormat initDateTimeFormat(String datePattern, String timePattern) {
        if (datePattern == null) {
            return null;
        }
        if (timePattern == null) {
            return DateTimeFormat.getFormat(datePattern);
        }

        return DateTimeFormat.getFormat(datePattern + timePattern);
    }

    public DatePickerPopup getDateBox() {
        return popupDatePicker;
    }
}
