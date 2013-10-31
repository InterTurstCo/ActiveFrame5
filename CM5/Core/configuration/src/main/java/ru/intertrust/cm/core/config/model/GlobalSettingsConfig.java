package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;
import ru.intertrust.cm.core.config.model.global.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.model.global.AttachmentUploadTempStorageConfig;

@Root(name = "global-settings")
public class GlobalSettingsConfig implements TopLevelConfig {
    /**
     * 
     */
    private static final long serialVersionUID = -8166587368979922484L;
    public static final String NAME = "global-settings";

    @Element(name = "audit-log", required = true)
    private AuditLog auditLog;

    @Element(name = "attachment-storage")
    private AttachmentStorageConfig attachmentStorageConfig;

    @Element(name = "attachment-upload-temp-storage")
    private AttachmentUploadTempStorageConfig attachmentUploadTempStorageConfig;


    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public AttachmentStorageConfig getAttachmentStorageConfig() {
        return attachmentStorageConfig;
    }

    public void setAttachmentStorageConfig(AttachmentStorageConfig attachmentStorageConfig) {
        this.attachmentStorageConfig = attachmentStorageConfig;
    }

    public AttachmentUploadTempStorageConfig getAttachmentUploadTempStorageConfig() {
        return attachmentUploadTempStorageConfig;
    }

    public void setAttachmentUploadTempStorageConfig(AttachmentUploadTempStorageConfig
                                                             attachmentUploadTempStorageConfig) {
        this.attachmentUploadTempStorageConfig = attachmentUploadTempStorageConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GlobalSettingsConfig that = (GlobalSettingsConfig) o;

        if (attachmentStorageConfig != null ? !attachmentStorageConfig.equals(that.
                attachmentStorageConfig) : that.attachmentStorageConfig != null) {
            return false;
        }
        if (attachmentUploadTempStorageConfig != null ? !attachmentUploadTempStorageConfig.
                equals(that.attachmentUploadTempStorageConfig) : that.attachmentUploadTempStorageConfig != null) {
            return false;
        }
        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = auditLog != null ? auditLog.hashCode() : 0;
        result = 31 * result + (attachmentStorageConfig != null ? attachmentStorageConfig.hashCode() : 0);
        result = 31 * result + (attachmentUploadTempStorageConfig != null ? attachmentUploadTempStorageConfig.
                hashCode() : 0);
        return result;
    }


    @Override
    public String getName() {
        return "";
    }
}
