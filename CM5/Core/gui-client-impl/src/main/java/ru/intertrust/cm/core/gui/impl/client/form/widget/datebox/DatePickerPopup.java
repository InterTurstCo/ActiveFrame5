package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

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

}
