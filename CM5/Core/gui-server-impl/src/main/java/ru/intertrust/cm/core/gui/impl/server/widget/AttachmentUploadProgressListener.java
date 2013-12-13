package ru.intertrust.cm.core.gui.impl.server.widget;

import org.apache.commons.fileupload.ProgressListener;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.12.13
 *         Time: 13:15
 */
public class AttachmentUploadProgressListener implements ProgressListener {
    private static final double COMPLETE_PERCENTAGE = 100d;
    private AttachmentUploadPercentage uploadProgress;

    public AttachmentUploadProgressListener(AttachmentUploadPercentage uploadProgress){
        this.uploadProgress = uploadProgress;
    }

    @Override
    public void update(long bytesRead, long totalBytes, int Items) {
        int percentage = (int) Math.floor(((double) bytesRead / (double) totalBytes) * COMPLETE_PERCENTAGE);
        uploadProgress.setPercentage(percentage);

    }
}
