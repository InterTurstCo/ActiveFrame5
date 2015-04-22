package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
public class AttachmentViewerState extends WidgetState {
    private String url;
    private String currentHeight;
    private String currentWidth;

    public AttachmentViewerState(){}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCurrentHeight() {
        return currentHeight;
    }

    public void setCurrentHeight(String currentHeight) {
        this.currentHeight = currentHeight;
    }

    public String getCurrentWidth() {
        return currentWidth;
    }

    public void setCurrentWidth(String currentWidth) {
        this.currentWidth = currentWidth;
    }
}
