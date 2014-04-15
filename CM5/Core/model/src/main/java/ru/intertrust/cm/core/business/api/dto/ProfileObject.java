package ru.intertrust.cm.core.business.api.dto;


public class ProfileObject extends BaseProfileObject implements Profile {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
