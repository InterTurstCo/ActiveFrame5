package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.AttachmentViewerRefConfig;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.10.13
 *         Time: 13:15
 */
public class AttachmentItem implements Dto {

    private String name;
    private String temporaryName;
    private String description;
    private String contentLength;
    private Id id;
    private String domainObjectType;
    //CMFIVE-3775
    private String mimeType;
    private AttachmentViewerRefConfig attachmentViewerRefConfig;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemporaryName() {
        return temporaryName;
    }

    public void setTemporaryName(String temporaryName) {
        this.temporaryName = temporaryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getTitle() {
        if (contentLength == null) {
           return name;
        } else {
            try {
                final long contentLengthLongValue = Long.parseLong(contentLength);
                final String formattedFileSize = StringUtil.getFormattedFileSize(contentLengthLongValue);

                String title = name + " (" + formattedFileSize + ")";
                return title;
            } catch (NumberFormatException ignored) {}
        }
        return name;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    //CMFIVE-3775
    public AttachmentViewerRefConfig getAttachmentViewerRefConfig() {
        return attachmentViewerRefConfig;
    }
    //CMFIVE-3775
    public void setAttachmentViewerRefConfig(AttachmentViewerRefConfig attachmentViewerRefConfig) {
        this.attachmentViewerRefConfig = attachmentViewerRefConfig;
    }
    //CMFIVE-3775
    public String getMimeType() {
        return mimeType;
    }
    //CMFIVE-3775
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttachmentItem that = (AttachmentItem) o;

        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (temporaryName != null ? !temporaryName.equals(that.temporaryName) : that.temporaryName != null) {
            return false;
        }
        if (contentLength != null ? !contentLength.equals(that.contentLength) : that.contentLength != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (temporaryName != null ? temporaryName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (contentLength != null ? contentLength.hashCode() : 0);
        return result;
    }
}
