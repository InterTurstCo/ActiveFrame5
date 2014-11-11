package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Lesia Puhova
 *         Date: 16.10.14
 *         Time: 15:33
 */
public class DeleteButtonPresenter implements AttachmentElementPresenter {

    private final AttachmentItem item;
    private final AttachmentElementPresenter presenter;
    private final DeleteButtonConfig deleteButtonConfig;
    private final ClickHandler deleteButtonHandler;

    public DeleteButtonPresenter(AttachmentElementPresenter presenter, AttachmentItem item, DeleteButtonConfig deleteButtonConfig,
                          ClickHandler deleteButtonHandler) {
        this.presenter = presenter;
        this.item = item;
        this.deleteButtonConfig = deleteButtonConfig;
        this.deleteButtonHandler = deleteButtonHandler;
    }

    @Override
    public Panel presentElement() {
        final Panel element = (presenter != null ? presenter.presentElement() : new AbsolutePanel());

        Command command = new Command("isDeletePermitted", "attachment-box", item.getId());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                if (Boolean.TRUE.equals(((BooleanValue) result).get())) {
                    if (displayDeleteButton()) {
                        Panel delBtn = new AbsolutePanel();

                        delBtn.addStyleName("facebook-btn");
                        delBtn.getElement().getStyle().clearPosition();

                        delBtn.addDomHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                element.removeFromParent();
                            }
                        }, ClickEvent.getType());
                        if (deleteButtonHandler != null) {
                            delBtn.addDomHandler(deleteButtonHandler, ClickEvent.getType());
                        }
                        element.add(delBtn);
                    }
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Exception while obtaining Delete access rights", caught);
            }
        });
        return element;
    }

    private boolean displayDeleteButton() {
        return deleteButtonConfig == null || deleteButtonConfig.isDisplay();
    }
}
