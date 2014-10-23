package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;

/**
 * @author Lesia Puhova
 *         Date: 16.10.14
 *         Time: 15:33
 */
class DeleteButtonPresenter implements AttachmentElementPresenter {

    private AttachmentElementPresenter presenter;
    private DeleteButtonConfig deleteButtonConfig;
    private ClickHandler deleteButtonHandler;

    DeleteButtonPresenter(AttachmentElementPresenter presenter,  DeleteButtonConfig deleteButtonConfig,
                          ClickHandler deleteButtonHandler) {
        this.presenter = presenter;
        this.deleteButtonConfig = deleteButtonConfig;
        this.deleteButtonHandler = deleteButtonHandler;
    }

    @Override
    public Panel presentElement() {
        final Panel element = (presenter != null ? presenter.presentElement() : new AbsolutePanel());
        if (displayDeleteButton()) {
            FocusPanel delBtn = new FocusPanel();
            delBtn.addStyleName("facebook-btn");
            delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
            delBtn.getElement().getStyle().setBackgroundColor("red");

            delBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    element.removeFromParent();
                }
            });
            if (deleteButtonHandler != null) {
                delBtn.addClickHandler(deleteButtonHandler);
            }
            element.add(delBtn);
        }
        return element;
    }

    private boolean displayDeleteButton() {
        //TODO: take into account 1) access 2) ref type (1:1, 1:n, n:m)
        return deleteButtonConfig == null || deleteButtonConfig.isDisplay();
    }
}
