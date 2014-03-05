package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.datepicker.client.DateBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.CMJDatePicker;

import java.util.Date;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * Created by User on 27.02.14.
 */
public class HeaderWidget {
    private CMJDatePicker picker;
    private DateBox dateBox;
    private String html;
    private String id;
    private String searchFilterName;
    private String fieldType;
    private String title;
    private boolean showFilter;

    private String filterValue = EMPTY_VALUE;

    public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    public HeaderWidget(String title, String fieldType, String searchFilterName, String id) {

        this.id = id;
        this.fieldType = fieldType;
        this.searchFilterName = searchFilterName;
        this.title = title;

    }

    public void init() {

        String styleDisplayingFilter = showFilter ? "" : " style=\"display:none\" ";
        String start = "<div class=\"header-label\"><p>" + title
                + "</p><p style=\"display:none\">" + title + ASCEND_ARROW
                + "</p><p style=\"display:none\">" + title + DESCEND_ARROW
                + " </p></div> <div class=\"search-container\"" +  styleDisplayingFilter +"id = ";
        if (DATE_TIME_TYPE.equalsIgnoreCase(fieldType)) {
            html = start + id + "><input type=\"text\" class=\"gwt-DateBox search-data-box\" id= " + id + "input" + " value= "+ filterValue + "><button type=\"button\" id= " + id + HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART +
                    " class=\"date-select\" ></button><button type=\"button\" class=\"search-box-clear-button-off\" id= " + id + HEADER_CLEAR_BUTTON_ID_PART + "></button></div>";
            picker = new CMJDatePicker();
            picker.toggle("date-picker-baloon","!!!");
            DateBox.Format format = new DateBox.DefaultFormat(DATE_TIME_FORMAT);
            dateBox = new DateBox(picker, null, format);
            initHandlers();
        } else {
            html = start + id + "><input type=\"text\" class=\"search-box\" maxlength=\"20\" id= " + id + "input"+ " value= "+ filterValue + "><button type=\"button\" + \" id= " + id + HEADER_CLEAR_BUTTON_ID_PART + " class=\"search-box-clear-button-off\"></button></div>";
        }
    }

    public String getId() {
        return id;
    }

    public String getHtml() {
        return html;
    }

   private void initHandlers() {

        dateBox.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                Date date = dateBox.getValue();

                InputElement.as(DOM.getElementById(id + HEADER_INPUT_ID_PART)).setValue(DATE_TIME_FORMAT.format(date));
            }
        });

    }

    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }

    public DateBox getDateBox() {
        return dateBox;
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

}
