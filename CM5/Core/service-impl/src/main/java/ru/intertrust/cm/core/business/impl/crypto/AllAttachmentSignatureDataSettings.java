package ru.intertrust.cm.core.business.impl.crypto;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.CollectorSettings;

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
}
