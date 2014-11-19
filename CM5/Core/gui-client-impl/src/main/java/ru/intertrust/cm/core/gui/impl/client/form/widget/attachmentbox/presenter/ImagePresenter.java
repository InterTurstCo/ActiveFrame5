package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.PreviewConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 13:59
 */
public class ImagePresenter implements AttachmentElementPresenter {

    private final PreviewConfig previewConfig;
    private final AttachmentItem item;
    private final PreviewConfig largePreviewConfig;
    private final List<AttachmentItem> attachments;

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;

    public ImagePresenter(AttachmentItem item, PreviewConfig previewConfig, PreviewConfig largePreviewConfig,
                          List<AttachmentItem> attachments) {
        this.item = item;
        this.previewConfig = previewConfig;
        this.largePreviewConfig = largePreviewConfig;
        this.attachments = attachments;
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
        takeHandlerPanel.add(image);
        image.addLoadHandler(new ScalePreviewHandler(previewConfig, image, true));

//        element.add(imageShadow);
        return element;
    }

    private ClickHandler getClickHandler(AttachmentItem item) {
        if (largePreviewConfig== null || !largePreviewConfig.isDisplay()) {
            return new DownloadAttachmentHandler(item);
        }
        return new OpenLargePreviewHandler(item, largePreviewConfig, attachments);
    }

    private static String createPreviewUrl(AttachmentItem item) {
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("attachment-download?");
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
        private final List<AttachmentItem> attachments;

        private AttachmentItem currentLargePreviewItem;
        private Panel prevButtonWrapper;
        private Panel nextButtonWrapper;

        public OpenLargePreviewHandler(AttachmentItem item, PreviewConfig largePreviewConfig,
                                       List<AttachmentItem> attachments) {
            this.item = item;
            this.config = largePreviewConfig;
            this.attachments = attachments;
            currentLargePreviewItem = item;
        }

        @Override
        public void onClick(ClickEvent event) {
            final DialogBox largePreviewDialog = new DialogBox(true, true);
            largePreviewDialog.setStyleName("popupWindow imageLargePreview");
            Panel largePreviewPanel = new AbsolutePanel();

            final Image image = new Image();
            image.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    largePreviewDialog.hide();
                }
            });
            setupLargePreviewImage(item, image);
            currentLargePreviewItem = item;
            largePreviewPanel.add(image);

            Button downloadButton = new Button("Загрузить");
            downloadButton.getElement().setClassName("darkButton");
            DownloadAttachmentHandler downloadHandler = new DownloadAttachmentHandler(item);
            downloadButton.addClickHandler(downloadHandler);

            largePreviewPanel.add(downloadButton);

            Panel buttonsPanel = new AbsolutePanel();
            buttonsPanel.getElement().setClassName("imageButtonWrapper");
            buttonsPanel.getElement().getStyle().clearPosition();
            prevButtonWrapper = new AbsolutePanel();
            prevButtonWrapper.getElement().setClassName("prevImageButtonWrapper");
            prevButtonWrapper.getElement().getStyle().clearPosition();
            Panel prevButton = new AbsolutePanel();
            prevButton.getElement().setClassName("prevImageButton");
            prevButton.getElement().getStyle().clearPosition();
            prevButtonWrapper.addDomHandler(new PrevClickHandler(image, largePreviewDialog, downloadHandler), ClickEvent.getType());
            prevButtonWrapper.add(prevButton);
            buttonsPanel.add(prevButtonWrapper);

            nextButtonWrapper = new AbsolutePanel();
            nextButtonWrapper.getElement().setClassName("nextImageButtonWrapper");
            nextButtonWrapper.getElement().getStyle().clearPosition();
            Panel nextButton = new AbsolutePanel();
            nextButton.getElement().setClassName("nextImageButton");
            nextButton.getElement().getStyle().clearPosition();
            nextButtonWrapper.addDomHandler(new NextClickHandler(image, largePreviewDialog, downloadHandler), ClickEvent.getType());
            nextButtonWrapper.add(nextButton);
            buttonsPanel.add(nextButtonWrapper);
            largePreviewPanel.add(buttonsPanel);

            ensureVisibilityForNavigationButtons();
            largePreviewDialog.setWidget(largePreviewPanel);
            largePreviewDialog.center();
        }

        private void setupLargePreviewImage(AttachmentItem item, Image image) {
            image.setUrl(createPreviewUrl(item));
            if (config.getWidth() != null) {
                image.setWidth(config.getWidth());
            }
            if (config.getHeight() != null) {
                image.setHeight(config.getHeight());
            }
            image.addLoadHandler(new ScalePreviewHandler(config, image, false));
        }

        private void showPrevImage(Image image) {
            int currentIndex = attachments.indexOf(currentLargePreviewItem);
            if (currentIndex > 0) {
                AttachmentItem prevItem = attachments.get(currentIndex-1);
                setupLargePreviewImage(prevItem, image);
                currentLargePreviewItem = prevItem;
                ensureVisibilityForNavigationButtons();
            }
        }

        private void showNextImage(Image image) {
            int currentIndex = attachments.indexOf(currentLargePreviewItem);
            if (currentIndex < attachments.size()-1) {
                AttachmentItem nextItem = attachments.get(currentIndex+1);
                setupLargePreviewImage(nextItem, image);
                currentLargePreviewItem = nextItem;
                ensureVisibilityForNavigationButtons();
            }
        }

        private void ensureVisibilityForNavigationButtons() {
            int currentIndex = attachments.indexOf(currentLargePreviewItem);
            prevButtonWrapper.setVisible(currentIndex > 0);
            nextButtonWrapper.setVisible(currentIndex < attachments.size() - 1);
        }


        private class PrevClickHandler implements com.google.gwt.event.dom.client.ClickHandler {
            private final Image image;
            private final DialogBox largePreviewDialog;
            private final DownloadAttachmentHandler downloadHandler;

            private PrevClickHandler(Image image, DialogBox largePreviewDialog,
                                     DownloadAttachmentHandler downloadHandler) {
                this.image = image;
                this.largePreviewDialog = largePreviewDialog;
                this.downloadHandler = downloadHandler;
            }

            @Override
            public void onClick(ClickEvent event) {
                showPrevImage(image);
                largePreviewDialog.center();
                downloadHandler.setItem(currentLargePreviewItem);
            }
        }

        private class NextClickHandler implements ClickHandler {
            private final Image image;
            private final DialogBox largePreviewDialog;
            private final DownloadAttachmentHandler downloadHandler;

            private NextClickHandler(Image image, DialogBox largePreviewDialog, DownloadAttachmentHandler
                    downloadHandler) {
                this.image = image;
                this.largePreviewDialog = largePreviewDialog;
                this.downloadHandler = downloadHandler;
            }

            @Override
            public void onClick(ClickEvent event) {
                showNextImage(image);
                largePreviewDialog.center();
                downloadHandler.setItem(currentLargePreviewItem);
            }
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
                // f.e. when we switch to another tab and save the form.
                if (maxWidth == 0 || maxHeight == 0) {
                    maxWidth = getPixelSize(previewConfig.getWidth(), DEFAULT_WIDTH);
                    maxHeight = getPixelSize(previewConfig.getHeight(), DEFAULT_HEIGHT);
                }

                image.setWidth("auto");
                image.setHeight("auto");
                int origWidth = image.getWidth();
                int origHeight = image.getHeight();

                int width = origWidth;
                int height = origHeight;
              //  if (origWidth > maxWidth) {
                    width = maxWidth;
                    height = origHeight * width / origWidth;
               // }
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

        private int getPixelSize(String sizeString, int defaultSize) {
            if (sizeString == null || !sizeString.endsWith("px")) {
                return defaultSize;
            }
            sizeString = sizeString.substring(0, sizeString.length() - "px".length());
            try {
                return Integer.parseInt(sizeString);
            } catch (NumberFormatException nfe) {
                return defaultSize;
            }
        }
    }

}
