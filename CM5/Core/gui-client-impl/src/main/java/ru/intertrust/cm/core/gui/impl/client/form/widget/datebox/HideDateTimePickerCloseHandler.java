package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.09.2014
 *         Time: 23:42
 */
public class HideDateTimePickerCloseHandler implements CloseHandler<PopupPanel> {
    private Panel container;

    protected HideDateTimePickerCloseHandler(Panel container) {
        this.container = container;
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        container.setStyleName("compositeDateTimeContainerHidden");
    }
}