package ru.intertrust.cm.core.config.importcsv;

import org.simpleframework.xml.Attribute;

public class DeleteAllConfig {
    @Attribute
    private String doel;

    public String getDoel() {
        return doel;
    }

    public void setDoel(String doel) {
        this.doel = doel;
    }
    
    
}
