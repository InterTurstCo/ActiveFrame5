package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.PreviewConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 13:59
 */
class ImagePresenter implements AttachmentElementPresenter {

    private static final String DEFAULT_WIDTH = "100px";
    private static final String DEFAULT_HEIGHT = "100px";

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
        Panel element = new AbsolutePanel();

        element.addStyleName("facebook-element");
        element.addStyleName("image-preview");
        Image image = new ScalableImage(previewConfig);
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("image-preview?");
        if (item.getId() != null) {
                url.append("id=").append(item.getId().toStringRepresentation());
        }
        if (item.getTemporaryName() != null) {
            url.append("tempName=").append(item.getTemporaryName());
        }
        image.setUrl(url.toString());
        if (clickHandler != null) {
            image.addClickHandler(clickHandler);
        }

        element.add(image);
//        element.setWidth(previewConfig.getWidth() != null ?  previewConfig.getWidth() : DEFAULT_WIDTH);
//        element.setHeight(previewConfig.getHeight() != null ? previewConfig.getHeight() : DEFAULT_HEIGHT);
        return element;
    }



    private static class ScalableImage extends Image {

        private PreviewConfig previewConfig;

        ScalableImage(PreviewConfig previewConfig) {
            this.previewConfig = previewConfig;
        }

        protected void onAttach(){
          //  Window.alert(getWidth() + " : " + getHeight());
            setupSizes(this, previewConfig);
            super.onAttach();
        }

        private static void setupSizes(Image image, PreviewConfig config) {
            if (!config.isPreserveProportion()) {
                image.setWidth(config.getWidth() != null ?  config.getWidth() : DEFAULT_WIDTH);
                image.setHeight(config.getHeight() != null ? config.getHeight() : DEFAULT_HEIGHT);
            } else {
                int origWidth = image.getWidth() != 0 ? image.getWidth() : 100; //TODO: const
                int origHeight = image.getHeight() != 0 ? image.getHeight() : 100;

                int width;
                int height;
                if (config.getWidth() == null && config.getHeight() == null) {
                    width = origWidth;
                    height = origHeight;
                } else if (config.getWidth() != null && config.getHeight() == null) {
                    image.setWidth(config.getWidth());
                    width = image.getWidth();
                    height = origHeight * width / origWidth;
                } else if (config.getHeight() != null && config.getWidth() == null) {
                    image.setHeight(config.getHeight());
                    height = image.getHeight();
                    width = origWidth * height / origHeight;
                } else {
                    image.setWidth(config.getWidth());
                    image.setHeight(config.getHeight());
                    int maxWidth = image.getWidth();
                    int maxHeight = image.getHeight();

                    width = maxWidth;
                    height = origHeight * width;
                    height = height / origWidth;

                    if (height > maxHeight) {
                        origWidth = width;
                        origHeight = height;

                        height = maxHeight;
                        width = origWidth * height / origHeight;
                    }
                }

                image.setPixelSize(width, height);
            }
        }
    }
}
