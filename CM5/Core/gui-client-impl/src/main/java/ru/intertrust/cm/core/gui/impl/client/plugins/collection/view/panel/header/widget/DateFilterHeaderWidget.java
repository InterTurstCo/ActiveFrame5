package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.DatePickerPopup;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.util.GuiConstants;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.HEADER_CLEAR_BUTTON_ID_PART;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants
        .HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 21:29
 */
public abstract class DateFilterHeaderWidget extends FilterHeaderWidget {
    protected DatePickerPopup popupDatePicker;
    protected String fieldType;
    protected String timePattern;
    protected DateTimeFormat userDateTimeFormat;
    protected DateTimeFormat guiDateTimeFormat;
    protected HandlerRegistration handlerRegistration;

    public DateFilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                  List<String> initialFilterValues, String valueSeparator) {
        super(column, columnProperties, initialFilterValues, valueSeparator);
        fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        timePattern = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        userDateTimeFormat = initUserDateTimeFormat(datePattern, timePattern);
        guiDateTimeFormat = initGuiDateTimeFormat();
    }

    protected void initHtml() {
        StringBuilder htmlBuilder = new StringBuilder(getTitleHtml());

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
        htmlBuilder.append(" class=\"");
        String clearButtonStyleClassName = filterValuesRepresentation.isEmpty() ? "search-box-clear-button-off"
                : GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn();
        htmlBuilder.append(clearButtonStyleClassName);
        htmlBuilder.append("\"></div></div>");

        html = htmlBuilder.toString();
    }

    private DateTimeFormat initUserDateTimeFormat(String datePattern, String timePattern) {
        if (datePattern == null) {
            return null;
        }
        if (timePattern == null) {
            return DateTimeFormat.getFormat(datePattern);
        }
        StringBuilder patternBuilder = new StringBuilder(datePattern);
        patternBuilder.append(" ");
        patternBuilder.append(timePattern);
        return DateTimeFormat.getFormat(patternBuilder.toString());
    }

    private DateTimeFormat initGuiDateTimeFormat() {
        return !FieldType.TIMELESSDATE.equals(FieldType.forTypeName(fieldType))
                ? DateTimeFormat.getFormat(GuiConstants.DATE_TIME_FORMAT)
                : DateTimeFormat.getFormat(GuiConstants.TIMELESS_DATE_FORMAT);

    }

    public DatePickerPopup getDateBox() {
        return popupDatePicker;
    }

    protected void removePreviousHandlerIfExist() {
        if (handlerRegistration == null) {
            return;
        }
        handlerRegistration.removeHandler();
    }

    protected boolean isShowTime() {
        return timePattern == null ? false : !FieldType.TIMELESSDATE.equals(FieldType.forTypeName(fieldType));
    }

    protected String convertUserDateStringToGuiDateString(String userDateString) {
        if (WidgetUtil.isStringEmpty(userDateString)) {
            return EMPTY_VALUE;
        }
        try {
            Date userDate = userDateTimeFormat.parse(userDateString);
            return guiDateTimeFormat.format(userDate);
        } catch (IllegalArgumentException ex) {
            String errorDescription = LocalizeUtil.get(BusinessUniverseConstants.DATE_FORMAT_ERROR) + getErrorMessage();
            ApplicationWindow.errorAlert(errorDescription);
            throw new GuiException(errorDescription);
        }
    }

    protected String getErrorMessage(){
        StringBuilder errorMessageBuilder = new StringBuilder(LocalizeUtil.get(BusinessUniverseConstants
                .WRONG_DATE_FORMAT_ERROR_MESSAGE));
        errorMessageBuilder.append(userDateTimeFormat.getPattern());
        return errorMessageBuilder.toString();
    }

}
