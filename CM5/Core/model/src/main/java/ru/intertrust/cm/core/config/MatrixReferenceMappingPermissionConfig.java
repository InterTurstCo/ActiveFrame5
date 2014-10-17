package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class MatrixReferenceMappingPermissionConfig implements Dto{
    private static final long serialVersionUID = 5569745532423272117L;

    @Attribute(name="map-from", required=true)
    private MatrixReferencePermissionEmum mapFrom;
    
    @Attribute(name="map-to", required=true)
    private MatrixReferencePermissionEmum mapTo;

    public MatrixReferencePermissionEmum getMapFrom() {
        return mapFrom;
    }

    public void setMapFrom(MatrixReferencePermissionEmum mapFrom) {
        this.mapFrom = mapFrom;
    }

    public MatrixReferencePermissionEmum getMapTo() {
        return mapTo;
    }

    public void setMapTo(MatrixReferencePermissionEmum mapTo) {
        this.mapTo = mapTo;
    }
    
    
}
