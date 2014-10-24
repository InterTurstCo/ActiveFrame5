package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.PreviewConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.ScalePreviewHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 13:59
 */
class ImagePresenter implements AttachmentElementPresenter {

    private PreviewConfig previewConfig;
    private ClickHandler clickHandler;
    private AttachmentItem item;

    ImagePresenter(AttachmentItem item, PreviewConfig previewConfig, ClickHandler clickHandler) {
        this.item = item;
        this.previewConfig = previewConfig;
        this.clickHandler = clickHandler;
    }

    @Override
    public Panel presentElement() {
        final Panel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        element.addStyleName("image-preview");

        if (previewConfig.getWidth() != null) {
            element.setWidth(previewConfig.getWidth());
        }
        if (previewConfig.getHeight() != null) {
            element.setHeight(previewConfig.getHeight());
        }

        final Image image = new Image(createPreviewUrl(item));
        image.addLoadHandler(new ScalePreviewHandler(previewConfig, image));
        if (clickHandler != null) {
            image.addClickHandler(clickHandler);
        }
        element.add(image);
        return element;
    }

    private static String createPreviewUrl(AttachmentItem item) {
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("image-preview?");
        if (item.getId() != null) {
            url.append("id=").append(item.getId().toStringRepresentation());
        }
        if (item.getTemporaryName() != null) {
            url.append("tempName=").append(item.getTemporaryName());
        }
        return url.toString();
    }

}
