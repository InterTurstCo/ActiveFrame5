package ru.intertrust.cm.core.config.model.gui;

import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class BeforeExecutionConfig implements Dto{

    @Element(name="confirmation-message")
    private ConfirmationMessageConfig confirmationMessage;

    public ConfirmationMessageConfig getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(ConfirmationMessageConfig confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
    
    
}
