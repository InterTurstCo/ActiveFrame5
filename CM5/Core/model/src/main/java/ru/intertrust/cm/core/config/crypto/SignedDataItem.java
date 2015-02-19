package ru.intertrust.cm.core.config.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedDataItem implements Dto {
    private Id id;
    private String name;
    private String content;
      
    public SignedDataItem() {
    }
    
    public SignedDataItem(Id id, String name, String content) {
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
