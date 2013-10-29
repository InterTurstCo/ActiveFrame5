package ru.intertrust.cm.core.config.model.gui;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class ActionContextActionConfig implements Dto{

    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
