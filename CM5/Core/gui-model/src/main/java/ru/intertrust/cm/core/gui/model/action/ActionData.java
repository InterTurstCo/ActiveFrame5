package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:16
 */
public class ActionData implements Dto {

    private String onSuccessMessage;
    private String onErrorMessage;

    public String getOnSuccessMessage() {
        return onSuccessMessage;
    }

    public void setOnSuccessMessage(String onSuccessMessage) {
        this.onSuccessMessage = onSuccessMessage;
    }

    public String getOnErrorMessage() {
        return onErrorMessage;
    }

    public void setOnErrorMessage(String onErrorMessage) {
        this.onErrorMessage = onErrorMessage;
    }
}
