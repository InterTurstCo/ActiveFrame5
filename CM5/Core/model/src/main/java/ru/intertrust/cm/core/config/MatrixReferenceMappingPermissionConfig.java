package ru.intertrust.cm.core.config;

import java.util.Objects;
import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class MatrixReferenceMappingPermissionConfig implements Dto{
    private static final long serialVersionUID = 5569745532423272117L;
    
    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String DELETE = "delete";
    public static final String CREATE_CHILD = "create-child";
    public static final String EXECUTE = "execute";

    @Attribute(name="map-from", required=true)
    private String mapFrom;
    
    @Attribute(name="map-to", required=true)
    private String mapTo;

    public String getMapFrom() {
        return mapFrom;
    }

    public void setMapFrom(String mapFrom) {
        this.mapFrom = mapFrom;
    }

    public String getMapTo() {
        return mapTo;
    }

    public void setMapTo(String mapTo) {
        this.mapTo = mapTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixReferenceMappingPermissionConfig that = (MatrixReferenceMappingPermissionConfig) o;
        return Objects.equals(mapFrom, that.mapFrom) &&
                Objects.equals(mapTo, that.mapTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapFrom, mapTo);
    }
}
