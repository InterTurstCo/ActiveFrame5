package ru.intertrust.cm.core.config;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name="File")
public class ImportFileConfig implements Dto {
    
    @Attribute
    private String name;
    
    @ElementList(inline=true, required=false, entry="Depend")
    private List<String> depend;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDepend() {
        return depend;
    }

    public void setDepend(List<String> depend) {
        this.depend = depend;
    }
}
