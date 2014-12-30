package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 29.12.14.
 */
public class CheckAccessResponse implements Dto {
    private boolean accessGranted = false;

    public void setAccessGranted(boolean accessGranted) {
        this.accessGranted = accessGranted;
    }

    public boolean isAccessGranted() {
        return accessGranted;
    }
}
