package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ImagesOnlyConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ActionPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.DeleteButtonPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ImagePresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.TextPresenter;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.11.14
 *         Time: 17:50
 */
public class EditablePresenterFactory implements AttachmentElementPresenterFactory {
    private final ActionLinkConfig actionLinkConfig;
    private final ImagesOnlyConfig imageConfig;
    private final DeleteButtonConfig deleteButtonConfig;

    public EditablePresenterFactory(ActionLinkConfig actionLinkConfig, ImagesOnlyConfig imageConfig,
                                    DeleteButtonConfig deleteButtonConfig) {
        this.actionLinkConfig = actionLinkConfig;
        this.imageConfig = imageConfig;
        this.deleteButtonConfig = deleteButtonConfig;
    }

    @Override
    public AttachmentElementPresenter createPresenter(final AttachmentItem item, ClickHandler deleteHandler,
                                                      List<AttachmentItem> attachments) {
        if (imageConfig != null) {
            return createEditableImagePresenter(item, deleteHandler, attachments);
        } else {
            return createEditableTextPresenter(item, deleteHandler);
        }
    }

    private AttachmentElementPresenter createEditableTextPresenter(final AttachmentItem item, ClickHandler deleteHandler) {
        AttachmentElementPresenter presenter = new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, deleteHandler);
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

    private AttachmentElementPresenter createEditableImagePresenter(final AttachmentItem item, ClickHandler deleteHandler,
                                                                    List<AttachmentItem> attachments) {
        AttachmentElementPresenter presenter = new ImagePresenter(item, imageConfig.getSmallPreviewConfig(),
                imageConfig.getLargePreviewConfig(), attachments);
        presenter = new DeleteButtonPresenter(presenter, item, deleteButtonConfig, deleteHandler);
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

}
