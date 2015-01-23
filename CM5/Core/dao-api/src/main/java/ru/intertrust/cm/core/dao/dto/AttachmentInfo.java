package ru.intertrust.cm.core.dao.dto;

/**
 * 
 * DTO, содержащий инфомацию и типе файла вложения (mimeType) и его размере (contentLength)
 * @author atsvetkov
 *
 */
public class AttachmentInfo {

    private String mimeType;
    private Long contentLength;
    
    private String relativePath;
    
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }        
}