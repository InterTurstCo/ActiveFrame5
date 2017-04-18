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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteAllConfig that = (DeleteAllConfig) o;

        if (doel != null ? !doel.equals(that.doel) : that.doel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return doel != null ? doel.hashCode() : 0;
    }
}
