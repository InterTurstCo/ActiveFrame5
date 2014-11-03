package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ImagesOnlyConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 17:18
 */
public class AttachmentElementPresenterFactory {

    private final List<AttachmentItem> attachments;
    private final ActionLinkConfig actionLinkConfig;
    private final ImagesOnlyConfig imageConfig;
    private final DeleteButtonConfig deleteButtonConfig;
    private final EventBus eventBus;

    public AttachmentElementPresenterFactory(List<AttachmentItem> attachments, ActionLinkConfig actionLinkConfig,
                                             ImagesOnlyConfig imageConfig, DeleteButtonConfig deleteButtonConfig,
                                             EventBus eventBus) {
        this.attachments = attachments;
        this.actionLinkConfig = actionLinkConfig;
        this.imageConfig = imageConfig;
        this.deleteButtonConfig = deleteButtonConfig;
        this.eventBus = eventBus;
    }

    public AttachmentElementPresenter createNonEditablePresenter(AttachmentItem item) {
        if (imageConfig != null) {
            return createNonEditableImagePresenter(item);
        } else {
            return createNonEditableTextPresenter(item);
        }
    }

    public List<AttachmentElementPresenter> createNonEditablePresenters(List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        if (imageConfig != null) {
            for (AttachmentItem item : items) {
                presenters.add(createNonEditableImagePresenter(item));
            }
        } else {
            for (AttachmentItem item : items) {
                presenters.add(createNonEditableTextPresenter(item));
            }
        }
        return presenters;
    }

    public AttachmentElementPresenter createEditablePresenter(final AttachmentItem item) {
        if (imageConfig != null) {
            return createEditableImagePresenter(item);
        } else {
            return createEditableTextPresenter(item);
        }
    }

    public List<AttachmentElementPresenter> createEditablePresenters(List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        if (imageConfig != null) {
            for (AttachmentItem item : items) {
                presenters.add(createEditableImagePresenter(item));
            }
        } else {
            for (AttachmentItem item : items) {
                presenters.add(createEditableTextPresenter(item));
            }
        }
        return presenters;
    }

    private AttachmentElementPresenter createNonEditableTextPresenter(AttachmentItem item) {
        return new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
    }

    private AttachmentElementPresenter createEditableTextPresenter(final AttachmentItem item) {
        AttachmentElementPresenter presenter = new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, new DeleteAttachmentClickHandler(item));
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

    public AttachmentElementPresenter createUploadPresenter(final AttachmentItem item, ClickHandler handler) {
        AttachmentElementPresenter presenter = new TextPresenter(item.getName());
        presenter = new UploadProgressPresenter(presenter, eventBus);
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, handler);
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

    private AttachmentElementPresenter createNonEditableImagePresenter(final AttachmentItem item) {
       return new ImagePresenter(item, imageConfig.getReadOnlyPreviewConfig(),
               getOnClickHandler(item));
    }

    private AttachmentElementPresenter createEditableImagePresenter(final AttachmentItem item) {
        AttachmentElementPresenter presenter = new ImagePresenter(item, imageConfig.getSmallPreviewConfig(),
                getOnClickHandler(item));
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, new DeleteAttachmentClickHandler(item));
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

    private ClickHandler getOnClickHandler(AttachmentItem item) {
        if (imageConfig == null) {
            return new DownloadAttachmentHandler(item);
        }
        if (imageConfig.getLargePreviewConfig() == null || !imageConfig.getLargePreviewConfig().isDisplay()) {
            return new DownloadAttachmentHandler(item);
        }
        return new ImagePresenter.OpenLargePreviewHandler(item, imageConfig.getLargePreviewConfig());
    }

    private class DeleteAttachmentClickHandler implements ClickHandler {

        private final AttachmentItem item;

        private DeleteAttachmentClickHandler(AttachmentItem item) {
            this.item = item;
        }

        @Override
        public void onClick(ClickEvent event) {
            attachments.remove(item);
        }
    }

}
