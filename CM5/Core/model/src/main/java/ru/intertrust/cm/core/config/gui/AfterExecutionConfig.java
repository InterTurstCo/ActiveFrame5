package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class AfterExecutionConfig implements Dto{

    @Element(name="on-success-message")
    private OnSuccessMessageConfig onSuccessMessage;

    public OnSuccessMessageConfig getOnSuccessMessage() {
        return onSuccessMessage;
    }

    public void setOnSuccessMessage(OnSuccessMessageConfig onSuccessMessage) {
        this.onSuccessMessage = onSuccessMessage;
    }
}
