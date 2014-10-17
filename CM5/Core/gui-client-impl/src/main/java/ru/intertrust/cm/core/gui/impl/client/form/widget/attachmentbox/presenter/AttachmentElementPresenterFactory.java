package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 17:18
 */
//TODO: this class will be changed to produce required presenter base on config
public class AttachmentElementPresenterFactory {

    private final List<AttachmentItem> attachments;
    private final ActionLinkConfig actionLinkConfig;
    private final EventBus eventBus;

    public AttachmentElementPresenterFactory(List<AttachmentItem> attachments, ActionLinkConfig actionLinkConfig,
                                             EventBus eventBus) {
        this.attachments = attachments;
        this.actionLinkConfig = actionLinkConfig;
        this.eventBus = eventBus;
    }

    private AttachmentElementPresenter createNonEditableTextPresenter(AttachmentItem item) {
        return new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
    }

    public List<AttachmentElementPresenter> createNonEditableTextPresenters(List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        for (AttachmentItem item : items) {
            presenters.add(createNonEditableTextPresenter(item));
        }
        return presenters;
    }

    public AttachmentElementPresenter createEditableTextPresenter(final AttachmentItem item) {
        TextPresenter textPresenter = new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
        DeleteButtonPresenter deleteButtonPresenter = new DeleteButtonPresenter(textPresenter, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachments.remove(item);
            }
        });
        return new ActionPresenter(deleteButtonPresenter, actionLinkConfig, item);
    }

    public List<AttachmentElementPresenter> createEditableTextPresenters(final List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        for (final AttachmentItem item : items) {
            presenters.add(createEditableTextPresenter(item));
        }
        return presenters;
    }

    public AttachmentElementPresenter createUploadPresenter(final AttachmentItem item, ClickHandler handler) {
        TextPresenter textPresenter = new TextPresenter(item.getName());
        UploadProgressPresenter inProgressPresenter = new UploadProgressPresenter(textPresenter, eventBus);
        DeleteButtonPresenter deleteButtonPresenter = new DeleteButtonPresenter(inProgressPresenter, handler);
        ActionPresenter actionPresenter = new ActionPresenter(deleteButtonPresenter, actionLinkConfig, item);
        return actionPresenter;
    }

}
