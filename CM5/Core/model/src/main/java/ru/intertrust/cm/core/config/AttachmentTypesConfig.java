package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * User: vlad
 */
public class AttachmentTypesConfig implements Dto  {
    @ElementList(entry="attachment-type", type=AttachmentTypeConfig.class, inline=true, required = true)
    private List<AttachmentTypeConfig> attachmentTypeConfigs = new ArrayList<AttachmentTypeConfig>();

    public List<AttachmentTypeConfig> getAttachmentTypeConfigs() {
        return attachmentTypeConfigs;
    }

    public void setAttachmentTypeConfigs(List<AttachmentTypeConfig> attachmentTypeConfigs) {
        if (attachmentTypeConfigs != null) {
            this.attachmentTypeConfigs = attachmentTypeConfigs;
        } else {
            this.attachmentTypeConfigs.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttachmentTypesConfig that = (AttachmentTypesConfig) o;

        if (attachmentTypeConfigs != null
                ? !attachmentTypeConfigs.equals(that.attachmentTypeConfigs) : that.attachmentTypeConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return attachmentTypeConfigs != null ? attachmentTypeConfigs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AttachmentTypesConfig [attachmentTypeConfigs=" + attachmentTypeConfigs + "]";
    }
    
    
}
