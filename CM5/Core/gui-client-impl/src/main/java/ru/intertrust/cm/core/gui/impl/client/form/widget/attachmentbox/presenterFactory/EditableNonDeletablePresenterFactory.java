package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ImagesOnlyConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ActionPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ImagePresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.TextPresenter;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 11.11.14
 *         Time: 17:54
 */
public class EditableNonDeletablePresenterFactory implements AttachmentElementPresenterFactory {

    private final ActionLinkConfig actionLinkConfig;
    private final ImagesOnlyConfig imageConfig;

    public EditableNonDeletablePresenterFactory(ActionLinkConfig actionLinkConfig, ImagesOnlyConfig imageConfig) {
        this.actionLinkConfig = actionLinkConfig;
        this.imageConfig = imageConfig;
    }

    @Override
    public AttachmentElementPresenter createPresenter(final AttachmentItem item) {
        if (imageConfig != null) {
            return createEditableImagePresenter(item);
        } else {
            return createEditableTextPresenter(item);
        }
    }

    @Override
    public AttachmentElementPresenter createPresenter(AttachmentItem item, ClickHandler handler) {
        return createPresenter(item);
    }

    private AttachmentElementPresenter createEditableTextPresenter(final AttachmentItem item) {
        AttachmentElementPresenter presenter = new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

    private AttachmentElementPresenter createEditableImagePresenter(final AttachmentItem item) {
        AttachmentElementPresenter presenter = new ImagePresenter(item, imageConfig.getSmallPreviewConfig(),
                imageConfig.getLargePreviewConfig());
        presenter = new ActionPresenter(presenter, actionLinkConfig, item);
        return presenter;
    }

}
