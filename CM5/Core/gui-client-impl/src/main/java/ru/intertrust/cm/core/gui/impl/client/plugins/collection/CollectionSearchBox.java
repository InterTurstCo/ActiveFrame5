package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.TableSearchEvent;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 17.12.13
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class CollectionSearchBox extends Composite implements IsWidget {
    public static enum Type {
        TEXTBOX, DATEBOX, CHECKBOX
    }

    private FlowPanel container = new FlowPanel();
    private FlowPanel boxContainer = new FlowPanel();
    private TextBox textBox;
    private DateBox dateBox;
    private FocusPanel close;
    private Button dateSet;
    private DatePicker datePicker;
    private Type type;
    private CheckBox checkBox;
    private String filterType;
    private String text = "";
    private Date date;
    private EventBus eventBus;
    private boolean escPress;

    public CollectionSearchBox(TextBox textBox, String filterType, EventBus eventBus) {
        createCommonElementCollectionSearchBox();
        this.textBox = textBox;
        this.type = Type.TEXTBOX;
        this.filterType = filterType;
        this.eventBus = eventBus;
        configTextBoxElement();
        escKeyPressHandler();

    }

    public CollectionSearchBox(DateBox dateBox, String filterType, EventBus eventBus) {
        createCommonElementCollectionSearchBox();
        this.dateBox = dateBox;
        this.type = Type.DATEBOX;
        this.filterType = filterType;
        dateSet = new Button();
        this.eventBus = eventBus;
        this.textBox = dateBox.getTextBox();
        configDateBoxElement();
        escKeyPressHandler();

    }

    private void configDateBoxElement() {
        boxContainer.add(dateBox);
        boxContainer.add(close);
        boxContainer.add(dateSet);
        dateSet.addStyleName("date-select");
        dateSet.removeStyleName("gwt-Button");
        dateBox.addStyleName("search-data-box");
        DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");
        dateBox.setFormat(new DateBox.DefaultFormat(format));
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
                text = "";
                dateBox.getTextBox().setValue("");
                close.addStyleName("flush-serch-hide");
                dateBox.removeStyleName("dateBoxFormatError");
            }
        });
        dateSet.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dateBox.showDatePicker();
                dateBox.getDatePicker().getParent().getElement().getStyle().setLeft(dateBox.getAbsoluteLeft() + 78, Style.Unit.PX);
                dateBox.getDatePicker().getParent().getElement().getStyle().setTop(dateBox.getAbsoluteTop() - 1, Style.Unit.PX);


            }
        });
        dateBox.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                date = dateBox.getValue();
                close.removeStyleName("flush-serch-hide");
            }
        });

        dateBox.addHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    text = dateBox.getTextBox().getText();
                    escPress = false;
                    fireEnterKey();
                }
            }
        }, KeyDownEvent.getType());

    }

    private void fireEnterKey() {

        eventBus.fireEvent(new TableSearchEvent(this));
    }

    private void configTextBoxElement() {
        textBox.addStyleName("search-box");
        textBox.removeStyleName("gwt-TextBox");
        textBox.setMaxLength(11);
        boxContainer.add(textBox);
        boxContainer.add(close);
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textBox.setText("");
                close.addStyleName("flush-serch-hide");
            }
        });

        textBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                text = textBox.getText();
                close.removeStyleName("flush-serch-hide");
            }
        });

        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    text = textBox.getText();
                    escPress = false;
                    fireEnterKey();
                }
            }
        });

    }

    private void confCloseButton() {
        close.addStyleName("flush-serch");
        close.removeStyleName("gwt-Button");

    }

    private void escKeyPressHandler(){
        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE){

                    escPress = true;
                    fireEnterKey();
                    textBox.setValue("");
                    text = "";
                }
            }
        });


    }



    private void createCommonElementCollectionSearchBox() {
        container.add(boxContainer);
        boxContainer.addStyleName("search-container");
        initWidget(container);
        close = new FocusPanel();
        close.addStyleName("flush-serch-hide");
        confCloseButton();

    }

    public FlowPanel getContainer() {
        return container;
    }

    public void setContainer(FlowPanel container) {
        this.container = container;
    }

    public TextBox getTextBox() {
        return textBox;
    }

    public void setTextBox(TextBox textBox) {
        this.textBox = textBox;
    }

    public DateBox getDateBox() {
        return dateBox;
    }

    public void setDateBox(DateBox dateBox) {
        this.dateBox = dateBox;
    }

    public FocusPanel getClose() {
        return close;
    }

    public void setClose(FocusPanel close) {
        this.close = close;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public void setDatePicker(DatePicker datePicker) {
        this.datePicker = datePicker;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public FlowPanel getBoxContainer() {
        return boxContainer;
    }

    public void setBoxContainer(FlowPanel boxContainer) {
        this.boxContainer = boxContainer;
    }

    public Button getDateSet() {
        return dateSet;
    }

    public void setDateSet(Button dateSet) {
        this.dateSet = dateSet;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public boolean isEscPress() {
        return escPress;
    }

    public void setEscPress(boolean escPress) {
        this.escPress = escPress;
    }
}
