package ru.intertrust.cm.core.gui.model.form.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
public class AttachmentViewerState extends WidgetState {
    private String url;
    private String currentHeight;
    private String currentWidth;
    private static List<String> EnabledTypes = new ArrayList<>(Arrays.asList("text/plain", "application/pdf"));
    private boolean commonUsage;
    private String downloadServletName;
    private String viewerId;

    public AttachmentViewerState() {
    }

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

    public static boolean isTypeEnabled(String mimeType) {
        if (mimeType == null || !EnabledTypes.contains(mimeType))
            return false;
        else
            return true;
    }

    public boolean isCommonUsage() {
        return commonUsage;
    }

    public void setCommonUsage(boolean commonUsage) {
        this.commonUsage = commonUsage;
    }

    public String getDownloadServletName() {
        return downloadServletName;
    }

    public void setDownloadServletName(String downloadServletName) {
        this.downloadServletName = downloadServletName;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }
}
