package ru.intertrust.cm.core.business.api.dto;


public class ProfileObject extends GenericIdentifiableObject implements Profile{

    private String name;

    private Id parent;

    @Override
    public Id getParent() {
        return parent;
    }

    public void setParent(Id parent) {
        this.parent = parent;
    }
    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
