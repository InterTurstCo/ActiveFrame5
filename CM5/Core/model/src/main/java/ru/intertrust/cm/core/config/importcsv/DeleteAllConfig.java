package ru.intertrust.cm.core.config.importcsv;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class DeleteAllConfig implements Dto {
    @Attribute
    private String doel;

    public String getDoel() {
        return doel;
    }

    public void setDoel(String doel) {
        this.doel = doel;
    }
    
    
}
