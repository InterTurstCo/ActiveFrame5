package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 0:42
 */
public class DatePickerPopup extends PopupPanel {
    protected EventBus eventBus;
    public DatePickerPopup(EventBus eventBus) {
        super(true, false);
        this.eventBus = eventBus;

    }

    protected Panel initDateSelector(String dateDescription, ClickHandler handler) {
        Panel panel = new AbsolutePanel();
        panel.setStyleName("composite-datetime-selection-item");
        Label label = new Label(dateDescription);
        panel.add(label);
        panel.addDomHandler(handler, ClickEvent.getType());
        return panel;

    }

    protected Panel initDateSelectorWithPicker(String dateDescription) {
        Panel panel = new AbsolutePanel();
        panel.setStyleName("composite-datetime-selection-item");
        Label label = new Label(dateDescription);
        panel.add(label);
        return panel;

    }

    protected class DatetimeClickHandler implements ClickHandler {
        private Panel container;

        protected DatetimeClickHandler(Panel container) {
            this.container = container;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            container.setStyleName("composite-date-time-container-shown");

        }
    }
    protected class HideDateTimePickerCloseHandler implements CloseHandler<PopupPanel> {
        private Panel container;

        protected HideDateTimePickerCloseHandler(Panel container) {
            this.container = container;
        }
        @Override
        public void onClose(CloseEvent<PopupPanel> event) {
            container.setStyleName("composite-date-time-container-shown");
        }
    }
}
