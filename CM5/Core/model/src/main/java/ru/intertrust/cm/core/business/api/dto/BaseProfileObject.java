package ru.intertrust.cm.core.business.api.dto;


public abstract class BaseProfileObject extends GenericIdentifiableObject{

    private Id parent;

    public Id getParent() {
        return parent;
    }

    public void setParent(Id parent) {
        this.parent = parent;
    }
}
