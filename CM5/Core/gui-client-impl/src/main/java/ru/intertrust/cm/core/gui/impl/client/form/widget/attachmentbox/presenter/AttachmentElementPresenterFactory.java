package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
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
//TODO: this class will be changed to produce required presenter based on config
public class AttachmentElementPresenterFactory {

    private final List<AttachmentItem> attachments;
    private final ActionLinkConfig actionLinkConfig;
    private final ImagesOnlyConfig imageConfig;
    private final EventBus eventBus;

    public AttachmentElementPresenterFactory(List<AttachmentItem> attachments, ActionLinkConfig actionLinkConfig,
                                             ImagesOnlyConfig imageConfig, EventBus eventBus) {
        this.attachments = attachments;
        this.actionLinkConfig = actionLinkConfig;
        this.imageConfig = imageConfig;
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

    private List<AttachmentElementPresenter> createNonEditableTextPresenters(List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        for (AttachmentItem item : items) {
            presenters.add(createNonEditableTextPresenter(item));
        }
        return presenters;
    }

    private AttachmentElementPresenter createEditableTextPresenter(final AttachmentItem item) {
        TextPresenter presenter = new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
        DeleteButtonPresenter deleteButtonPresenter = new DeleteButtonPresenter(presenter, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachments.remove(item);
            }
        });
        return new ActionPresenter(deleteButtonPresenter, actionLinkConfig, item);
    }

    private List<AttachmentElementPresenter> createEditableTextPresenters(final List<AttachmentItem> items) {
        List<AttachmentElementPresenter> presenters = new ArrayList<>(items.size());
        for (final AttachmentItem item : items) {
            presenters.add(createEditableTextPresenter(item));
        }
        return presenters;
    }

    public AttachmentElementPresenter createUploadPresenter(final AttachmentItem item, ClickHandler handler) {
        TextPresenter presenter = new TextPresenter(item.getName());
        UploadProgressPresenter inProgressPresenter = new UploadProgressPresenter(presenter, eventBus);
        DeleteButtonPresenter deleteButtonPresenter = new DeleteButtonPresenter(inProgressPresenter, handler);
        ActionPresenter actionPresenter = new ActionPresenter(deleteButtonPresenter, actionLinkConfig, item);
        return actionPresenter;
    }

    private AttachmentElementPresenter createNonEditableImagePresenter(final AttachmentItem item) {
       return new ImagePresenter(item, imageConfig.getReadOnlyPreviewConfig(),
               new DownloadAttachmentHandler(item)); //TODO: open large preview handler
    }

    private AttachmentElementPresenter createEditableImagePresenter(final AttachmentItem item) {
        ImagePresenter presenter = new ImagePresenter(item, imageConfig.getSmallPreviewConfig(),
                new DownloadAttachmentHandler(item)); //TODO: open large preview handler
        DeleteButtonPresenter deleteButtonPresenter = new DeleteButtonPresenter(presenter, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachments.remove(item);
            }
        });
        return new ActionPresenter(deleteButtonPresenter, actionLinkConfig, item);
    }
}
