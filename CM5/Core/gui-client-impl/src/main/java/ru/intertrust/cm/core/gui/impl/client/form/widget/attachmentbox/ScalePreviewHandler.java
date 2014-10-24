package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import ru.intertrust.cm.core.config.gui.form.widget.PreviewConfig;

/**
 * @author Lesia Puhova
 *         Date: 24.10.14
 *         Time: 20:02
 */
public class ScalePreviewHandler implements LoadHandler {

    final private PreviewConfig previewConfig;
    final private Image image;

    public ScalePreviewHandler(PreviewConfig previewConfig, Image image) {
        this.previewConfig = previewConfig;
        this.image = image;
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

            image.setWidth("auto");
            image.setHeight("auto");
            int origWidth = image.getWidth();
            int origHeight = image.getHeight();

            int width = maxWidth;
            int height = origHeight * width;
            height = height / origWidth;

            if (height > maxHeight) {
                origWidth = width;
                origHeight = height;

                height = maxHeight;
                width = origWidth * height / origHeight;
            }
            image.setPixelSize(width, height);
        }
    }

}
