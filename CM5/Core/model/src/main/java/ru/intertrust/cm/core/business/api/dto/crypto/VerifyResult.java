package ru.intertrust.cm.core.business.api.dto.crypto;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Структура для передачи информации о валидности ЭЦП
 * @author larin
 *
 */
public class VerifyResult implements Dto{
    private static final long serialVersionUID = -7995499379143873587L;
    private boolean valid;
    private List<String> messages;
    private List<String> warnings;
    private List<String> errors;
    private String signer;
    
    public List<String> getMessages() {
        return messages;
    }
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    public List<String> getWarnings() {
        return warnings;
    }
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    public List<String> getErrors() {
        return errors;
    }
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    public String getSigner() {
        return signer;
    }
    public void setSigner(String signer) {
        this.signer = signer;
    }
}
