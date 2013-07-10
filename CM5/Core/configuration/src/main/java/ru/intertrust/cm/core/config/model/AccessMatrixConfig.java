package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "accessMatrix")
public class AccessMatrixConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    @Element(name = "status")
    private AccessMatrixStatusConfig status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AccessMatrixStatusConfig getStatus() {
        return status;
    }

    public void setStatus(AccessMatrixStatusConfig status) {
        this.status = status;
    }

}
