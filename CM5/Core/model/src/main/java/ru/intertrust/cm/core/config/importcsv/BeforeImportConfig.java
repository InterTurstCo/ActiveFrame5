package ru.intertrust.cm.core.config.importcsv;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class BeforeImportConfig implements Dto {
    @Attribute(name="import-type")
    private String importType;
    
    @ElementList(entry="delete-all", required=false, inline=true)
    private List<DeleteAllConfig> deleteAll;

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public List<DeleteAllConfig> getDeleteAll() {
        return deleteAll;
    }

    public void setDeleteAll(List<DeleteAllConfig> deleteAll) {
        this.deleteAll = deleteAll;
    }
    
}
