package ru.intertrust.cm.core.gui.model.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class GuiSignedDataItem implements Dto{
    private static final long serialVersionUID = -3132225111239223064L;
    private Id id;
    private String name;
    private String content;
      
    public GuiSignedDataItem() {
    }
    
    public GuiSignedDataItem(Id id, String name, String content) {
        super();
        this.id = id;
        this.name = name;
        this.content = content;
    }
    public Id getId() {
        return id;
    }
    public void setId(Id id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
