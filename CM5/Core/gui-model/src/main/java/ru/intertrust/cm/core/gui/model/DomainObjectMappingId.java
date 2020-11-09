package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Id;

public class DomainObjectMappingId implements Id {
    private String type;
    private String id;

    public DomainObjectMappingId(){
    }

    public DomainObjectMappingId(String type, String id){
        this.type = type;
        this.id = id;
    }

    @Override
    public void setFromStringRepresentation(String stringRep) {
        String[] idPair = stringRep.split(":");
        type = idPair[0];
        id = idPair[1];
    }

    @Override
    public String toStringRepresentation() {
        return type + ":" + id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
