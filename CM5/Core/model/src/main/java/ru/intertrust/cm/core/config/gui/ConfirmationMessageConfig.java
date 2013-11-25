package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class ConfirmationMessageConfig implements Dto{
    @Attribute
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
