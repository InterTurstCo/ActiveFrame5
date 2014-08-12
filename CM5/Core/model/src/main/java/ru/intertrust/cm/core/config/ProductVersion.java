package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 31.07.2014.
 */
public class ProductVersion implements Dto{

    @Attribute(name = "archive", required = false)
    private String archive;

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductVersion that = (ProductVersion) o;

        if (archive != null ? !archive.equals(that.archive) : that.archive != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return archive != null ? archive.hashCode() : 0;
    }
}
