package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:16
 */
public class ActionData implements Dto {

    private String onSuccessMessage;

    public String getOnSuccessMessage() {
        return onSuccessMessage;
    }

    public void setOnSuccessMessage(String onSuccessMessage) {
        this.onSuccessMessage = onSuccessMessage;
    }


}
