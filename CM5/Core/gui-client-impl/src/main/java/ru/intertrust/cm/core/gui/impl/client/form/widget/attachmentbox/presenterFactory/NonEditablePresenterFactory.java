package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import ru.intertrust.cm.core.config.gui.form.widget.DigitalSignaturesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ImagesOnlyConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.DigitalSignaturePresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.ImagePresenter;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.TextPresenter;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.11.14
 *         Time: 16:04
 */
public class NonEditablePresenterFactory implements AttachmentElementPresenterFactory {

    private final ImagesOnlyConfig imageConfig;
    private final DigitalSignaturesConfig digitalSignaturesConfig;

    public NonEditablePresenterFactory(ImagesOnlyConfig imageConfig, DigitalSignaturesConfig digitalSignaturesConfig) {
        this.imageConfig = imageConfig;
        this.digitalSignaturesConfig = digitalSignaturesConfig;
    }

    @Override
    public AttachmentElementPresenter createPresenter(AttachmentItem item, ClickHandler handler, List<AttachmentItem> attachments) {
        AttachmentElementPresenter presenter;
        if (imageConfig != null) {
            presenter = createNonEditableImagePresenter(item, attachments);
        } else {
            presenter = createNonEditableTextPresenter(item);
        }
        presenter = new DigitalSignaturePresenter(presenter, item, digitalSignaturesConfig);
        return presenter;
    }

    private AttachmentElementPresenter createNonEditableImagePresenter(final AttachmentItem item, List<AttachmentItem
            > attachments) {
        return new ImagePresenter(item, imageConfig.getReadOnlyPreviewConfig(),
                imageConfig.getLargePreviewConfig(), attachments);
    }

    private AttachmentElementPresenter createNonEditableTextPresenter(AttachmentItem item) {
        return new TextPresenter(item.getTitle(), new DownloadAttachmentHandler(item));
    }
}
