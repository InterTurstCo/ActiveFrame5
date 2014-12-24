package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 21.12.14.
 */
public class LinkedTableActionResponse implements Dto {
    private boolean accessGranted = true;

    public void setAccessGranted(boolean accessGranted) {
        this.accessGranted = accessGranted;
    }

    public boolean isAccessGranted() {
        return accessGranted;
    }
}
