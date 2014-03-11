package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;


public class BeforeExecutionConfig implements Dto {

    @Element(name="confirmation-message")
    private ConfirmationMessageConfig confirmationMessage;

    @Element(name="validatorsConfig", required = false)
    private ValidatorsConfig validatorsConfig;

    public ConfirmationMessageConfig getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(ConfirmationMessageConfig confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public ValidatorsConfig getValidatorsConfig() {
        return validatorsConfig;
    }

    public void setValidatorsConfig(ValidatorsConfig validatorsConfig) {
        this.validatorsConfig = validatorsConfig;
    }
}
