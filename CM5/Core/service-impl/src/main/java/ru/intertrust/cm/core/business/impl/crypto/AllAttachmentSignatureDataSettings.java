package ru.intertrust.cm.core.business.impl.crypto;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.CollectorSettings;

import java.util.List;

@Root(name="all-attachment-signature")
public class AllAttachmentSignatureDataSettings implements CollectorSettings{
    private static final long serialVersionUID = -1441247577378015801L;
    
    @ElementList(entry="exlude-attachment-type", required=false, inline=true)
    private List<String> exludeAttachmentType;
    
    @ElementList(name="exlude-attachment-name", required=false, inline=true)
    private List<String> exludeAttachmentName;
    
    public List<String> getExludeAttachmentType() {
        return exludeAttachmentType;
    }
    public void setExludeAttachmentType(List<String> exludeAttachmentType) {
        this.exludeAttachmentType = exludeAttachmentType;
    }
    public List<String> getExludeAttachmentName() {
        return exludeAttachmentName;
    }
    public void setExludeAttachmentName(List<String> exludeAttachmentName) {
        this.exludeAttachmentName = exludeAttachmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AllAttachmentSignatureDataSettings that = (AllAttachmentSignatureDataSettings) o;

        if (exludeAttachmentType != null ? !exludeAttachmentType.equals(that.exludeAttachmentType) : that.exludeAttachmentType != null)
            return false;
        if (exludeAttachmentName != null ? !exludeAttachmentName.equals(that.exludeAttachmentName) : that.exludeAttachmentName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return exludeAttachmentName != null ? exludeAttachmentName.hashCode() : 0;
    }
}
