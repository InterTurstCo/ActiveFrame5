package ru.intertrust.cm.core.gui.model.action.infobar;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by Vitaliy Orlov on 09.08.2016.
 */
public class InfoBarItem implements Dto{
    private String name;
    private String value;

    public InfoBarItem() {
    }

    public InfoBarItem(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
