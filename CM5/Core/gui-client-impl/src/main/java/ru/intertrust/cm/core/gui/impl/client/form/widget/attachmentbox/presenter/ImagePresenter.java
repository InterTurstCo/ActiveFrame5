package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.PreviewConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 13:59
 */
public class ImagePresenter implements AttachmentElementPresenter {

    private final PreviewConfig previewConfig;
    private final AttachmentItem item;
    private final PreviewConfig largePreviewConfig;

    public ImagePresenter(AttachmentItem item, PreviewConfig previewConfig, PreviewConfig largePreviewConfig) {
        this.item = item;
        this.previewConfig = previewConfig;
        this.largePreviewConfig = largePreviewConfig;
    }

    @Override
    public Panel presentElement() {
        final Panel element = new AbsolutePanel();
//        final Panel imageShadow = new AbsolutePanel();
        element.addStyleName("facebook-element");
        element.addStyleName("imagePreview");
//        imageShadow.setStyleName("imageShadow");

        if (previewConfig.getWidth() != null) {
            element.setWidth(previewConfig.getWidth());
        }
        if (previewConfig.getHeight() != null) {
            element.setHeight(previewConfig.getHeight());
        }
        Panel takeHandlerPanel = new AbsolutePanel();
        takeHandlerPanel.addStyleName("imagePreviewHandlerPanel");
        //if (clickHandler != null) {
            takeHandlerPanel.addDomHandler(getClickHandler(item), ClickEvent.getType());
         //}
        element.add(takeHandlerPanel);
        final Image image = new Image(createPreviewUrl(item));
        image.addLoadHandler(new ScalePreviewHandler(previewConfig, image, true));

        takeHandlerPanel.add(image);
//        element.add(imageShadow);
        return element;
    }

    private ClickHandler getClickHandler(AttachmentItem item) {
        if (largePreviewConfig== null || !largePreviewConfig.isDisplay()) {
            return new DownloadAttachmentHandler(item);
        }
        return new OpenLargePreviewHandler(item, largePreviewConfig);
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

    public static class OpenLargePreviewHandler implements ClickHandler {
        private final AttachmentItem item;
        private final PreviewConfig config;

        public OpenLargePreviewHandler(AttachmentItem item, PreviewConfig largePreviewConfig) {
            this.item = item;
            this.config = largePreviewConfig;
        }

        @Override
        public void onClick(ClickEvent event) {
            final DialogBox largePreviewDialog = new DialogBox(true, true);
            largePreviewDialog.setStyleName("popupWindow imageLargePreview");
            Image image = new Image(createPreviewUrl(item));

            image.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    largePreviewDialog.hide();
                }
            });
            if (config.getWidth() != null) {
                image.setWidth(config.getWidth());
            }
            if (config.getHeight() != null) {
                image.setHeight(config.getHeight());
            }
            image.addLoadHandler(new ScalePreviewHandler(config, image, false));
            largePreviewDialog.setWidget(image);
            largePreviewDialog.center();
        }
    }

    private static class ScalePreviewHandler implements LoadHandler {

        final private PreviewConfig previewConfig;
        final private Image image;
        final private boolean center;

        public ScalePreviewHandler(PreviewConfig previewConfig, Image image, boolean center) {
            this.previewConfig = previewConfig;
            this.image = image;
            this.center = center;
        }

        @Override
        public void onLoad(LoadEvent event) {
            setupSizes();
        }

        private  void setupSizes() {
            if (!previewConfig.isPreserveProportion()) {
                image.setWidth(previewConfig.getWidth() != null ? previewConfig.getWidth() : "100%");
                if (previewConfig.getHeight() != null) {
                    image.setHeight(previewConfig.getHeight());
                }
            } else {
                int maxWidth = image.getParent().getOffsetWidth();
                int maxHeight = image.getParent().getOffsetHeight();

                // workaround for the situation when the attachment-box widget is being updated while it's out of vision,
                // f.e. when we switch to another tab and save the form. Let's just skip scaling for now.
                if (maxWidth == 0 || maxHeight == 0) {
                    return; // TODO: Need to find better solution.
                }

                image.setWidth("auto");
                image.setHeight("auto");
                int origWidth = image.getWidth();
                int origHeight = image.getHeight();

                int width = origWidth;
                int height = origHeight;
                if (origWidth > maxWidth) {
                    width = maxWidth;
                    height = origHeight * width / origWidth;
                }
                if (height > maxHeight) {
                    origWidth = width;
                    origHeight = height;

                    height = maxHeight;
                    width = origWidth * height / origHeight;
                }
                image.setPixelSize(width, height);
                if (center) {
                    if (width < maxWidth) {
                        image.getElement().getStyle().setProperty("paddingLeft", (maxWidth-width)/2 + "px");
                    }
                    if (height < maxHeight) {
                        image.getElement().getStyle().setProperty("paddingTop", (maxHeight-height)/2 + "px");
                    }
                }
            }

        }
    }
}
