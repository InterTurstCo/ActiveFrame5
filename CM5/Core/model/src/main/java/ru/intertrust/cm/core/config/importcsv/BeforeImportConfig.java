package ru.intertrust.cm.core.config.importcsv;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeforeImportConfig that = (BeforeImportConfig) o;

        if (importType != null ? !importType.equals(that.importType) : that.importType != null) return false;
        if (deleteAll != null ? !deleteAll.equals(that.deleteAll) : that.deleteAll != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return importType != null ? importType.hashCode() : 0;
    }
}
