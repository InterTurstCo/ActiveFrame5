package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.impl.client.form.widget.CMJDatePicker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.PopupDatePicker;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.Date;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/14
 *         Time: 12:05 PM
 */
public class HeaderWidget {
    private CMJDatePicker picker;
    private PopupDatePicker popupDatePicker;
    private String html;
    private String id;
    private String searchFilterName;
    private String fieldType;
    private String title;
    private boolean showFilter;
    private String fieldName;
    private String filterValue = EMPTY_VALUE;

   private DateTimeFormat dateTimeFormat;

    public HeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties ) {

        title = column.getDataStoreName();
        searchFilterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
        fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        id = (column.hashCode() + title).replaceAll(" ", "");
        fieldName = (String) columnProperties.getProperty(CollectionColumnProperties.FIELD_NAME);
        if (datePattern != null) {
        dateTimeFormat = DateTimeFormat.getFormat(datePattern);
        }
    }

    public void init() {
        String styleDisplayingFilter = showFilter ? "" : " style=\"display:none\" ";
        String minWidthStyle = "" /*"style = \"width:" + minWidth +"px\" "*/;

        html = "<div  "+ minWidthStyle +" class=\"header-label\"><p>" + title
                + "</p><p style=\"display:none\">" + title + ASCEND_ARROW
                + "</p><p style=\"display:none\">" + title + DESCEND_ARROW
                + " </p></div>";
        if (searchFilterName == null) {
            return;
        }
        String filterAreaStart = " <div class=\"search-container\"" + styleDisplayingFilter + "id = ";
        String clearButtonClass = filterValue.isEmpty() ?  " class=\"search-box-clear-button-off\"></div></div>" : " class=\"search-box-clear-button-on\"></div></div>";
        if (TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType) || DATE_TIME_TYPE.equalsIgnoreCase(fieldType)) {

            html = html + filterAreaStart + id + ">" + "<input type=\"text\" class=\"gwt-DateBox search-data-box\" id= " + id + "input "
                    + "><button type=\"button\" id= " + id + HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART
                    + " class=\"date-select\" ></button><div type=\"button\" id= "
                    + id + HEADER_CLEAR_BUTTON_ID_PART + clearButtonClass;
            picker = new CMJDatePicker();
            picker.toggle("date-picker-baloon", "!!!");

            popupDatePicker = new PopupDatePicker(picker)  ;
            initHandlers();
        } else {
            html = html + filterAreaStart + id + ">" + "<input type=\"text\" class=\"search-box\" maxlength=\"20\" id= " + id + "input "
                    + "><div type=\"button\" + \" id= "
                    + id + HEADER_CLEAR_BUTTON_ID_PART + clearButtonClass;
        }
    }

    public String getId() {
        return id;
    }

    public String getHtml() {
        return html;
    }

    private void initHandlers() {

        popupDatePicker.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                Date date = popupDatePicker.getDatePicker().getValue();
                String dateValue = dateTimeFormat.format(date);
                setFilterValue(dateValue);
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).focus();
                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(dateValue);
                DOM.getElementById(id + HEADER_CLEAR_BUTTON_ID_PART).setClassName("search-box-clear-button-on");
            }
        });

    }

    public boolean isShowFilter() {
        return showFilter;
    }

    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }

    public PopupDatePicker getDateBox() {
        return popupDatePicker;
    }

    public String getSearchFilterName() {
        return searchFilterName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public DateTimeFormat getDateTimeFormat() {
        return dateTimeFormat;
    }
}
