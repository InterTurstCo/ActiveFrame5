package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="File")
public class ImportFileConfig {
    
    @Attribute
    private String name;
    
    @ElementList(inline=true, required=false)
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
