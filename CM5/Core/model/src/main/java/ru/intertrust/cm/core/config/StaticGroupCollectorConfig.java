package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name ="group")
public class StaticGroupCollectorConfig implements CollectorSettings{

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
