package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * Created by lvov on 02.04.14.
 */
public class CancelHeaderNotificationItem implements Dto {

    private Id id;
    private ArrayList<HeaderNotificationItem> items;

    public CancelHeaderNotificationItem(Id id) {
        this.id = id;
    }

    public CancelHeaderNotificationItem() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public ArrayList<HeaderNotificationItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<HeaderNotificationItem> items) {
        this.items = items;
    }

}
